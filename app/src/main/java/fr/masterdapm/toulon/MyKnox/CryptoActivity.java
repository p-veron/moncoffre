package fr.masterdapm.toulon.MyKnox;

import android.app.Activity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import fr.masterdapm.toulon.MyKnox.lib.MyCipher;

public class CryptoActivity extends Activity {

	private MyCipher myCipher;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crypto);
		myCipher= new MyCipher();
		Log.d("Varkal", "Let's go !");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Call the chiffre method and set all visible result in the gui
	 * @param v Required for button onclick param
	 */
	public void encrypt(View v){
		String message = ((EditText)findViewById(R.id.et_message)).getText().toString();
		String password = ((EditText)findViewById(R.id.et_password)).getText().toString();		
		byte[] byteResult = myCipher.chiffre(password.toCharArray(), message.getBytes());
		setKey();
		setResult(byteResult);
		setMessage("".getBytes());		
	}
	
	/**
	 * Set the key fiel in gui
	 */
	public void setKey(){
		EditText key_et = ((EditText)findViewById(R.id.et_key));
		key_et.setText(Base64.encodeToString(myCipher.getKey().getEncoded(), Base64.DEFAULT|Base64.NO_PADDING|Base64.NO_WRAP));
	}
	
	/**
	 * Set the result field in gui
	 * @param cryptogram The cryptogram to show in gui
	 */
	public void setResult(byte[] cryptogram){
		EditText result_et = ((EditText)findViewById(R.id.et_result));
		result_et.setText(Base64.encodeToString(cryptogram, Base64.DEFAULT|Base64.NO_PADDING|Base64.NO_WRAP));;
	}
	
	/**
	 * Set the message in gui
	 * @param message The message to show
	 */
	public void setMessage(byte[] message){
		EditText message_et = ((EditText)findViewById(R.id.et_message));
		if(message!=null){
			message_et.setText(new String(message));
		} else {
			message_et.setText("ERROR !!");
		}
		
	}
	
	/**
	 * Call the dechiffre method and set all the visible result in gui
	 * @param v Require for the onclick butonn
	 */
	public void decrypt(View v){
		String result = ((EditText)findViewById(R.id.et_result)).getText().toString();
		String password = ((EditText)findViewById(R.id.et_password)).getText().toString();		
		byte[] byteMessage = myCipher.dechiffre(password.toCharArray(), Base64.decode(result, Base64.DEFAULT|Base64.NO_PADDING|Base64.NO_WRAP));
		setMessage(byteMessage);
	}
}
