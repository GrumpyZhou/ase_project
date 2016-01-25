package de.tum.score.transport4you.mobile.presentation.presentationmanager.impl;

import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import de.tum.score.transport4you.mobile.R;
import de.tum.score.transport4you.mobile.application.applicationcontroller.IMainApplication;
import de.tum.score.transport4you.mobile.application.applicationcontroller.impl.ApplicationSingleton;
import de.tum.score.transport4you.mobile.presentation.presentationmanager.IPresentation;
import de.tum.score.transport4you.mobile.shared.MobileIntents;

public class MainScreen extends Activity implements IPresentation{
	private String ticket = "unmodified";
    private String username = "unmodified";
    private String password = "unmodified";
	private Context currentContext;
	private IMainApplication mainApplication;
	private Button synchronizeButton;
	private HashMap<String,View> GUIElements = new HashMap<String, View>();
	
	private ProgressDialog busConnectionProgressDialog;
	static final int busConnection_DIALOG = 0;
	
	private ProgressDialog synchronizeProgressDialog;
	static final int synchronize_DIALOG = 1;
	
	private ProgressDialog activeProgressDialog;
	static int active_DIALOG = -1;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        currentContext = this;

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            ticket = extras.getString("KEY");
            username = extras.getString("username");
            password = extras.getString("password");

        }


        mainApplication = ApplicationSingleton.getApplicationController();
        mainApplication.registerActivity(this);
        
        Button showSettingsButton = (Button) findViewById(R.id.btn_settings);        
        showSettingsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
	        	Intent newIntent = new Intent(currentContext, SettingsScreen.class);
	            startActivity(newIntent);  
            }
        });
    
        Button showAccountDetailsButton = (Button) findViewById(R.id.btn_accountdetails);        
        showAccountDetailsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
	        	Intent newIntent = new Intent(currentContext, AccountDetailsScreen.class);
	            startActivity(newIntent);    
            }
        });
        
        Button showETicketListButton = (Button) findViewById(R.id.btn_etickets);        
        showETicketListButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
	        	Intent newIntent = new Intent(currentContext, ETicketListScreen.class);
	            startActivity(newIntent);    
            }
        });
        
        synchronizeButton = (Button) findViewById(R.id.btn_synchronize);  
        GUIElements.put("synchronizeButton",synchronizeButton);
        synchronizeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
	        	showDialog(synchronize_DIALOG);
	        	//synchronizeButton.setEnabled(false);
            }
        });
        
/*        startBusScanButton = (Button) findViewById(R.id.btn_startbusscan);
        GUIElements.put("startBusScanButton",startBusScanButton);
        startBusScanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
	        	showDialog(busConnection_DIALOG);
	        	//startBusScanButton.setEnabled(false);
            }
        })*/
        
    }
	
    public void onDestroy() {
    	super.onDestroy();
		//kills all opended broadcast receivers
    	Intent tearDown = new Intent(MobileIntents.TEAR_DOWN);
    	currentContext.sendBroadcast(tearDown);
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
	
	protected Dialog onCreateDialog(int id) {
		switch(id) {
	        case busConnection_DIALOG:
	            busConnectionProgressDialog = new ProgressDialog(MainScreen.this);
	            return busConnectionProgressDialog;
	        case synchronize_DIALOG:
	            synchronizeProgressDialog = new ProgressDialog(MainScreen.this);
	            return synchronizeProgressDialog;
	        default:
	            return null;
        }
	}
	
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id) {
	        case busConnection_DIALOG:
		        busConnectionProgressDialog.setMessage("Scanning...");
		        busConnectionProgressDialog.setIndeterminate(true);
		        activeProgressDialog = busConnectionProgressDialog;
		        active_DIALOG = id;
			        new Thread(){
			        	public void run() {
				        	mainApplication.startBusScan();  
			        	}
			        }.start();
			        break;
			case synchronize_DIALOG:
				synchronizeProgressDialog.setMessage("Synchronizing...");
				synchronizeProgressDialog.setIndeterminate(true);
		        activeProgressDialog = synchronizeProgressDialog;
		        active_DIALOG = id;
			        new Thread(){
			        	public void run() {
				        	mainApplication.synchronize(); 
			        	}
			        }.start();
		        break;
		}
	}
	
	// Define the Handler that receives messages from the thread and update the progress
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
        	if (activeProgressDialog == null)
        			return;
        	if (msg.arg1 == 0) {
        		activeProgressDialog.hide();
        		activeProgressDialog = null;
        		active_DIALOG = -1;
        		return;
        	}
        	if (msg.obj != null && msg.obj instanceof String)
        		activeProgressDialog.setMessage((CharSequence) msg.obj);
        }
    };
    
    @Override
	public void updateProgessDialog(String title, String message, boolean visible, Integer increment) {
    	Message msg = handler.obtainMessage();
        msg.arg1 = visible ? 1 : 0;
        msg.arg2 = increment;
        msg.obj = message;
        handler.sendMessage(msg);
	}

	public void showQRCode(View v){
		System.out.println("ticket" + ticket);
		System.out.println("username " + username);
        System.out.println("password " + password);

        String toShow = "http://score-1042.appspot.com/rest/"
                + "user/" +
                username + "/" + password;

        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });

        //String content= ticket;
        ImageView imageView = new ImageView(this);
        try {
            imageView.setImageBitmap(de.tum.score.transport4you.mobile.qrcode.QRCode.createQRCode(toShow));
        }
        catch(Exception e){
            e.printStackTrace();
        }
		//imageView.setImageBitmap();

        //imageView.setImageURI(new URI("222"));
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
	}
}