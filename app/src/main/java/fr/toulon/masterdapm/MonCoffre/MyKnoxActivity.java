package fr.toulon.masterdapm.MonCoffre;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import fr.toulon.masterdapm.MonCoffre.lib.MyCipher;
import fr.toulon.masterdapm.MonCoffre.lib.PasswordLog;
import fr.toulon.masterdapm.MonCoffre.lib.PasswordLogDataSource;

public class MyKnoxActivity extends Activity {


	private MyCipher cipher;
	private char[] password;
	private PasswordLogDataSource datasource;
	private ProgressDialog progress;
    private File filetemp;
	/* variable utilis'ee pour le contr^ole de la validit'e du mot de passe */
	private byte[] checkpasswd={(byte)0xff,(byte)0,(byte)0xff,(byte)0,(byte)0xff,(byte)0,(byte)0xff,(byte)0,(byte)0xff,(byte)0,(byte)0xff,(byte)0,(byte)0xff,(byte)0,(byte)0xff,(byte)0};

	@Override
	
	protected void onDestroy()
	{
		super.onDestroy();
		if (password != null)
		Arrays.fill(password, (char) 0);		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d("masterdapm.MonCoffre", "masterdapm.MonCoffre Start");


		cipher = new MyCipher();
		datasource = new PasswordLogDataSource(getApplicationContext());
		if (datasource.isempty()) {
			passwordChooseDialog();
		} else {
			passwordAskDialog();
		}

	}

	public void goToList(View v) {
		Intent intent = new Intent(this, ListActivity.class);
		intent.putExtra("password", password);
		startActivity(intent);
	}

	
	public void apropos(View v){
		Intent intent = new Intent(this, Apropos.class);
		startActivity(intent);	
	}
	/**
	 * Ouvre la popup demandant le mot de passe au commencement de l'appli Puis,
	 * si l'utilisateur avait d'eja enregistr'e des donn'ees, v'erifie si ce mot de
	 * passe est le bon. Si oui, ou si il n'y avait pas de donn'ees ce mot de
	 * passe est choisi comme mot de passe courrant.
	 */
	public void passwordAskDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View dialogPasswordView = inflater.inflate(R.layout.dialog_login, null);
		builder.setTitle(R.string.dialog_password_title);
		builder.setIcon(android.R.drawable.ic_menu_help);
		builder.setView(dialogPasswordView).setPositiveButton(R.string.validate_label, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {

				Editable EtempPassword  = ((EditText) dialogPasswordView.findViewById(R.id.dialog_et_password)).getText();
				
				final char[]	tempPassword = new char[EtempPassword.toString().length()];
				EtempPassword.toString().getChars(0, tempPassword.length, tempPassword, 0);

				if ((tempPassword.length != 0)&& !tempPassword.equals(null)) {
					setPassword(tempPassword);
					if (!tryDecrypt()) {
						// Mauvais mot de passe. On pr'evient l'utilisateur
						new AlertDialog.Builder(MyKnoxActivity.this).setTitle(R.string.dialog_passwordError_title)
								.setMessage(R.string.dialog_passwordError_message)
								.setPositiveButton(R.string.validate_label, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										passwordAskDialog();
									}
								}).setIcon(android.R.drawable.ic_dialog_alert).setCancelable(false).show();
					}

				} else {
					// Si mot de passe vide, on lui réaffiche le
					// formulaire
					passwordAskDialog();
				}
			}
		});
		builder.setCancelable(false);
		builder.create().show();
	}

	public void passwordChooseDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View dialogPasswordView = inflater.inflate(R.layout.dialog_choose_password, null);
		builder.setTitle(R.string.dialog_password_title);
		builder.setIcon(android.R.drawable.ic_menu_help);
		builder.setView(dialogPasswordView).setPositiveButton(R.string.validate_label, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {

				Editable EtempPassword1 = ((EditText) dialogPasswordView.findViewById(R.id.dialog_et_password1)).getText();
				Editable EtempPassword2  = ((EditText) dialogPasswordView.findViewById(R.id.dialog_et_password2)).getText();
				
				char[]	tempPassword1 = new char[EtempPassword1.toString().length()];
				char[]	tempPassword2 = new char[EtempPassword2.toString().length()];
				
				EtempPassword1.toString().getChars(0, tempPassword1.length, tempPassword1, 0);
				EtempPassword2.toString().getChars(0, tempPassword2.length, tempPassword2, 0);
				if ((tempPassword1.length != 0) && (tempPassword2.length != 0)) {
					if (Arrays.equals(tempPassword1,tempPassword2)) {
						setPassword(tempPassword2);
						Arrays.fill(tempPassword1, (char) 0);
						byte[] crypto = cipher.chiffre(password, checkpasswd);
						/* on met 0 en d'ebut du nom pour etre sur que ce sera le premier site dans la base */
						PasswordLog site = new PasswordLog((char) 0x0 +(char)0x0 + (char)0x0 +(char)0x0 +"________", crypto);
						datasource.open();
						datasource.insert(site);
						datasource.close();

					} else {
						new AlertDialog.Builder(MyKnoxActivity.this).setTitle(R.string.passwordNoMatch_title)
								.setMessage(R.string.passwordNoMatch_message)
								.setPositiveButton(R.string.validate_label, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										passwordChooseDialog();
									}
								}).setIcon(android.R.drawable.ic_dialog_alert).setCancelable(false).show();
					}
				} else {
					new AlertDialog.Builder(MyKnoxActivity.this).setTitle(R.string.passwordEmpty_title)
							.setMessage(R.string.passwordEmpty_message)
							.setPositiveButton(R.string.validate_label, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									passwordChooseDialog();
								}
							}).setIcon(android.R.drawable.ic_dialog_alert).setCancelable(false).show();
				}
			}
		});
		builder.setCancelable(false);
		builder.create().show();
	}

	/**
	 * Permet d'ajouter un site dans la base
	 * @param v Requis pour le onClick du bouton dans le layout
	 */
	public void addSiteDialog(View v) {
		
		
		final View paramview = v;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View addSiteView = inflater.inflate(R.layout.dialog_add, null);
		builder.setTitle(R.string.button_add_label);
		builder.setIcon(android.R.drawable.ic_menu_add);
		builder.setView(addSiteView).setPositiveButton(R.string.validate_label, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				
				
				String siteName = ((EditText) addSiteView.findViewById(R.id.dialog_add_et_name)).getText().toString();
				Editable Elogin = ((EditText) addSiteView.findViewById(R.id.dialog_add_et_login)).getText();
				Editable EPassword = ((EditText) addSiteView.findViewById(R.id.dialog_add_et_password)).getText();
				
				byte[] siteLogin = null;
				byte[] sitePassword = null;

				try {
					siteLogin = Elogin.toString().getBytes("UTF-8");
				} catch (UnsupportedEncodingException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}
				
				try {
					sitePassword = EPassword.toString().getBytes("UTF-8");
				} catch (UnsupportedEncodingException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}
				
				// On ajoute le site dans la base que s'il ne contient pas de champs vides
				if (!siteName.equals("") && (siteLogin.length != 0) && (sitePassword.length != 0)) {
					ByteBuffer plainText = ByteBuffer.allocate(siteLogin.length+sitePassword.length+1);
					plainText.put((byte)siteLogin.length);
					plainText.put(siteLogin);
					plainText.put(sitePassword);
					Arrays.fill(siteLogin, (byte) 0);
					Arrays.fill(sitePassword, (byte) 0);
					
					byte[] crypto = cipher.chiffre(password, plainText.array());
					
					/* on efface tout en memoire */
					plainText.rewind();
					plainText.put((byte) 0);
					plainText.put(siteLogin,0,siteLogin.length);
					plainText.put(sitePassword,0,sitePassword.length);

					PasswordLog site = new PasswordLog(siteName, crypto);
					datasource.open();
					datasource.insert(site);
					datasource.close();
				}
				else
				{
					int Alert_message ;
					if (siteName.equals(""))
					{
						Alert_message = R.string.empty_site;
					}
					else if (siteLogin.length == 0)
					{
						Alert_message = R.string.empty_login;
					}
					else
					{
						Alert_message = R.string.empty_passwd;
					}									
					new AlertDialog.Builder(addSiteView.getContext())
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.incomplete_Field)
					.setMessage(Alert_message)
					.setPositiveButton(R.string.validate_label,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									addSiteDialog(paramview);
								}

							}).show();

				}

			}
		}).setNegativeButton(R.string.cancel_label, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		builder.create().show();
	}

	/**
	 * Permet à l'utilisateur de changer de mot de passe. Decrypte puis
	 * réencrypte toutes les données avec le nouveau mot de passe
	 * 
	 * @see MyKnoxActivity.reencryptAllPass
	 * @param v Requis pour le onClick du bouton dans le layout
	 */
	public void changePassword(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View dialogPasswordView = inflater.inflate(R.layout.dialog_change_password, null);
		builder.setTitle(R.string.dialog_password_title);
		builder.setIcon(android.R.drawable.ic_menu_help);
		builder.setView(dialogPasswordView).setPositiveButton(R.string.validate_label, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				Editable EtryPassword = ((EditText) dialogPasswordView.findViewById(R.id.dialog_et_oldPassword)).getText();
				char[]	tryPassword = new char[EtryPassword.toString().length()];
				
				EtryPassword.toString().getChars(0, tryPassword.length, tryPassword, 0);
				if (Arrays.equals(tryPassword,password)) {
					Arrays.fill(tryPassword, (char) 0);
					Editable EtempPassword1 = ((EditText) dialogPasswordView.findViewById(R.id.dialog_et_password1)).getText();
					Editable EtempPassword2  = ((EditText) dialogPasswordView.findViewById(R.id.dialog_et_password2)).getText();
					
					char[]	tempPassword1 = new char[EtempPassword1.toString().length()];
					char[]	tempPassword2 = new char[EtempPassword2.toString().length()];
					
					EtempPassword1.toString().getChars(0, tempPassword1.length, tempPassword1, 0);
					EtempPassword2.toString().getChars(0, tempPassword2.length, tempPassword2, 0);

					if ((tempPassword1.length != 0) && (tempPassword2.length != 0)) {
						if (Arrays.equals(tempPassword1,tempPassword2)) {
							//Lancé dans un Thread séparé pour ne pas freezer l'UI
							Arrays.fill(tempPassword1, (char) 0);
							Thread process = new Thread(new ReencryptProcess(tempPassword2));
							progress = new ProgressDialog(MyKnoxActivity.this);
							progress.setTitle(R.string.progress_title);
							progress.setMessage(MyKnoxActivity.this.getString(R.string.progress_message));
							process.start();
							progress.show();
						} else {
							new AlertDialog.Builder(MyKnoxActivity.this).setTitle(R.string.passwordNoMatch_title)
									.setMessage(R.string.passwordNoMatch_message)
									.setPositiveButton(R.string.validate_label, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											changePassword(dialogPasswordView);
										}
									}).setIcon(android.R.drawable.ic_dialog_alert).setCancelable(false).show();
						}
					} else {
						new AlertDialog.Builder(MyKnoxActivity.this).setTitle(R.string.passwordEmpty_title)
								.setMessage(R.string.passwordEmpty_message)
								.setPositiveButton(R.string.validate_label, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										changePassword(dialogPasswordView);
									}
								}).setIcon(android.R.drawable.ic_dialog_alert).setCancelable(false).show();
					}
				} else {
					new AlertDialog.Builder(MyKnoxActivity.this).setTitle(R.string.dialog_passwordError_title)
							.setMessage(R.string.dialog_passwordError_message)
							.setPositiveButton(R.string.validate_label, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									changePassword(dialogPasswordView);
								}
							}).setIcon(android.R.drawable.ic_dialog_alert).setCancelable(false).show();
				}
			}
		}).setNegativeButton(R.string.cancel_label, null);
		builder.setCancelable(false);
		builder.create().show();
	}

	/**
	 * Vide complétement la base de données après avoir avertit l'utilisateur
	 * 
	 * @param v
	 *            Requis pour le onClick du bouton dans le layout
	 */
	public void removeAll(View v) {
		new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.removeAll_title)
				.setMessage(R.string.removeAll_message).setPositiveButton(R.string.validate_label, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						datasource.open();
						datasource.deleteAll();
						datasource.close();
						byte[] crypto = cipher.chiffre(password, checkpasswd);
						PasswordLog site = new PasswordLog((char) 0x0 +(char)0x0 + (char)0x0 +(char)0x0 +"________", crypto);
						datasource.open();
						datasource.insert(site);
						datasource.close();
					}

				}).setNegativeButton(R.string.cancel_label, null).show();
	}

	/**
	 * Appelé par changePassword Reencrypte toute la base de données avec un
	 * nouveau mot de passe
	 * 
	 * @param newPassword
	 *            Le nouveau mot de passe
	 */
	public void reencryptAllPass(char[] newPassword) {
		datasource.open();

		List<PasswordLog> siteList = datasource.getAllFrom(0);
		for (PasswordLog site : siteList) {
			byte[] plaintext = cipher.dechiffre(password, site.getCrypto());
			byte[] crypto = cipher.chiffre(newPassword, plaintext);
			site.setCrypto(crypto);
			datasource.update(site.getSiteName(),site);
		}
		datasource.close();
		password = Arrays.copyOf(newPassword, newPassword.length);
		Arrays.fill(newPassword, (char) 0);
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(char[] password) {
		this.password = password;
	}

	/**
	 * Essaye de d'ecrypter le premier enregistrement de la base avec le mot de
	 * passe fournit par l'utilisateur
	 * 
	 * @return True si tous c'est bien passé, False sinon
	 */
	public boolean tryDecrypt() {

		datasource.open();
		PasswordLog pl = datasource.getAllFrom(0).get(0);
		datasource.close();
		byte[] crypto = pl.getCrypto();
		byte[] info = cipher.dechiffre(password, crypto);
		return Arrays.equals(info, checkpasswd);
	}

	/**
	 * Appelé pendant changePassword Fait tourner le procces de réencryption
	 * pendant que l'UI affiche un loader
	 **/
	private class ReencryptProcess implements Runnable {
		private char[] newPassword;

		public ReencryptProcess(char[] newPassword) {
			super();
			this.newPassword = newPassword;
		}

		@Override
		public void run() {
			reencryptAllPass(newPassword);
			progress.dismiss();
		}

	}

    public void writeToExternal(String filename){
        try {
            filetemp = new File(getExternalFilesDir(null), filename);
            File file = Environment.getDataDirectory();
            String pathToMyAttachedFile = "data/fr.toulon.masterdapm.MonCoffre/databases/passwords.db";
            file = new File(file, pathToMyAttachedFile);
            InputStream is = new FileInputStream(file);
            OutputStream os = new FileOutputStream(filetemp);
            byte[] toWrite = new byte[is.available()]; //Init a byte array for handing data transfer
         //   Log.i("Available ", is.available() + "");
            int result = is.read(toWrite); //Read the data from the byte array
         //   Log.i("Result", result + "");
            os.write(toWrite); //Write it to the output stream
            is.close(); //Close it
            os.close(); //Close it
         //   Log.i("Copying to", "" +getExternalFilesDir(null) + File.separator + filename);
         //   Log.i("Copying from", getFilesDir() + File.separator + filename + "");
        } catch (Exception e) {
            Toast.makeText(this, "File write failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show(); //if there's an error, make a piece of toast and serve it up
        }
    }


	public void writeFromExternal(String filename){
		try {
            File file = new File(getExternalFilesDir(null), filename);
            filetemp = Environment.getDataDirectory();
            String pathToMyAttachedFile = "data/fr.toulon.masterdapm.MonCoffre/databases/passwords.db";
			filetemp = new File(filetemp, pathToMyAttachedFile);
			InputStream is = new FileInputStream(file);
			OutputStream os = new FileOutputStream(filetemp);
			byte[] toWrite = new byte[is.available()]; //Init a byte array for handing data transfer
			Log.i("Available ", is.available() + "");
			int result = is.read(toWrite); //Read the data from the byte array
			Log.i("Result", result + "");
			os.write(toWrite); //Write it to the output stream
			is.close(); //Close it
			os.close(); //Close it
            Toast.makeText(this, "Restauration OK", Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(this, "File write failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show(); //if there's an error, make a piece of toast and serve it up
		}
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        Log.d("KNOX",Integer.toString(requestCode)+" "+Integer.toString(resultCode));
        if (requestCode == 12) {
               // filetemp.delete();
        }
    }

    public void do_backup(String email)
    {
        String [] tab_email = new String[1];
        tab_email[0] = email;
        writeToExternal("passwords.db.sc");
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, tab_email);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Sauvegarde BD Mon Coffre");
        //	emailIntent.putExtra(Intent.EXTRA_TEXT, "body text");
        filetemp = Environment.getExternalStorageDirectory();
        Log.d("KNOX",filetemp.toString());
        String pathToMyAttachedFile = "Android/data/fr.toulon.masterdapm.MonCoffre/files/passwords.db.sc";
        filetemp = new File(filetemp, pathToMyAttachedFile);
        if (!filetemp.exists())
        {
            Log.d("KNOX","No file");
            Toast.makeText(this,"Fichier passwords.ds.sc inexistant",Toast.LENGTH_LONG).show();
            return;
        }
        if (!filetemp.canRead()) {
            Log.d("KNOX","No Read");
            Toast.makeText(this,"Erreur lecture passwords.ds.sc",Toast.LENGTH_LONG).show();
            return;
        }
        Uri uri = Uri.fromFile(filetemp);
        String mimeType = getContentResolver().getType(uri);
        Log.d("KOX","Mime : "+mimeType);
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        //  emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(Intent.createChooser(emailIntent, "Pick an Email provider"),12);
    }

    public void backup(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogEmailView = inflater.inflate(R.layout.dialog_email, null);

        Log.d("MonCoffre","OK");
        builder.setView(dialogEmailView)
                .setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.backup)
                .setPositiveButton(R.string.validate_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String adr_email = ((EditText) dialogEmailView.findViewById(R.id.dialog_et_email)).getText().toString();
                Log.d("MonCoffre",adr_email);
                if (!adr_email.equals(""))
                    do_backup(adr_email);
                else
                    Toast.makeText(getBaseContext(),"Le champ email ne peut pas être vide",Toast.LENGTH_LONG).show();
            }

        })
                .setNegativeButton(R.string.cancel_label, null).show();

    }

	/* TODO */
   // public void restore(View v)
    //{
      //  writeFromExternal("passwords.db.sc");
      //  Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
      //  Uri uri = Uri.parse(Environment.getExternalStorageDirectory().toString()); // a directory
      // intent.setDataAndType(uri, "*/*");
       // intent.setData(uri);
       // startActivity(Intent.createChooser(intent, "Open folder"));
    //}

}
