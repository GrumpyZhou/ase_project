package de.tum.score.transport4you.mobile.presentation.presentationmanager.impl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.bouncycastle.util.encoders.Base64;
import org.restlet.resource.ClientResource;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import de.tum.score.transport4you.mobile.R;
import de.tum.score.transport4you.mobile.application.applicationcontroller.IMainApplication;
import de.tum.score.transport4you.mobile.application.applicationcontroller.impl.ApplicationSingleton;
import de.tum.score.transport4you.mobile.communication.dataconnectioncontroller.impl.DataConnectionController;
import de.tum.score.transport4you.mobile.presentation.presentationmanager.IPresentation;
import de.tum.score.transport4you.shared.mobilebusweb.data.impl.BlobEnvelope;
import de.tum.score.transport4you.shared.mobileweb.impl.message.MobileSettings;

public class LoginScreen extends Activity implements IPresentation{
    
	private IMainApplication mainApplication;
	private Context currentContext;
	
	private TextView usernameText;
	private TextView passwordText;
	private CheckBox rememberLoginCheckBox;
	
	private String username;
	private String password;
	private boolean rememberLogin;
	private MobileSettings mobileSettings;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        currentContext = this;
        
        
        //create ApplicationController and register activity
        mainApplication = ApplicationSingleton.getApplicationController();
        mainApplication.registerActivity(this);
        mainApplication.initialize(currentContext);
        
        usernameText = (TextView) findViewById(R.id.txt_username);
        passwordText = (TextView) findViewById(R.id.txt_password);
        rememberLoginCheckBox = (CheckBox) findViewById(R.id.chk_rememberpassword);
        
        mobileSettings = mainApplication.getMobileSettings();
        
        //check if remember login is set and fill out form
        if (mobileSettings.isRememberLogin()) {
        	rememberLoginCheckBox.setChecked(true);
        	usernameText.setText(mobileSettings.getUsername());
        	passwordText.setText(mobileSettings.getPassword());
        }
        
        Button loginButton = (Button) findViewById(R.id.btn_login);        
        loginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                username = usernameText.getText().toString();
            	password = passwordText.getText().toString();
            	rememberLogin = rememberLoginCheckBox.isChecked();
            	
            	//only start main activity if user is logged in
                if (mainApplication.login(username, password, rememberLogin)) {
                	Intent mainIntent = new Intent(currentContext, MainScreen.class);

                    String xml = null;
                    String ticketString = null;
                    try {
                        xml = new ClientResource("http://score-1042.appspot.com/rest/"
                                + "user/" +
                                username + "/" +
                                DataConnectionController.computeMD5(password)).get().getText();


                        InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));

                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document document = builder.parse(is);

                        XPath xpath = XPathFactory.newInstance().newXPath();

                        ticketString = (String) xpath.evaluate("/user/ticket", document, XPathConstants.STRING);

/*                        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(Base64.decode(ticketString.getBytes()));
                        ObjectInput in = new ObjectInputStream(byteInputStream);
                        BlobEnvelope result = (BlobEnvelope) in.readObject();
*/
                    }
                    catch(Exception e) {

                    }
					mainIntent.putExtra("KEY",ticketString);
                    mainIntent.putExtra("username", username);
                    mainIntent.putExtra("password", DataConnectionController.computeMD5(password));
                	startActivity(mainIntent);
                    //finish();
                } else {
                	Toast toast = Toast.makeText(currentContext, "Login failed - provided wrong credentials?", Toast.LENGTH_SHORT);
                	toast.show();
                }
                
            }
        });
    }
	
    public void onDestroy() {
    	super.onDestroy();
    }

	@Override
	public void shutdown() {
		this.finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.mn_exit:
	        mainApplication.shutdownSystem();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void updateProgessDialog(String title, String message, boolean visible, Integer increment) {
		// TODO Auto-generated method stub
		
	}
	
}