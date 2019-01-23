package fr.toulon.masterdapm.MonCoffre;


import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import fr.toulon.masterdapm.MonCoffre.lib.MyCipher;
import fr.toulon.masterdapm.MonCoffre.lib.PasswordLog;
import fr.toulon.masterdapm.MonCoffre.lib.PasswordLogDataSource;

public class ListActivity extends Activity implements OnItemClickListener {

	private ListView listview;
	private char[] password;
	private PasswordLogDataSource datasource;

	private MyCipher cipher;

    private int laposition;

    private MyDialog InfoDialog;
	private PasswordLog[] values;
    private byte[] text;

    public static class MyDialog extends DialogFragment implements OnClickListener{
        /* classe pour boite de dialogue personnalisée avec images à la place des boutons */

        public MyDialog()
        {
        }

        public static MyDialog newInstance(String letitre, int icon, int layout_button, byte[] infos_connexion) {
            MyDialog frag = new MyDialog();
            Bundle args = new Bundle();
            args.putString("Titre", letitre);
            args.putInt("R_icon",icon);
            args.putInt("R_layout_button",layout_button);
            args.putByteArray("Infos",infos_connexion);
            frag.setArguments(args);
            return frag;
        }


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){

            View Mydiagview ;
            AlertDialog.Builder builder;
            String titre;
            int R_icon, R_layout_button;
            byte[] infos;

            titre = getArguments().getString("Titre");
            R_icon = getArguments().getInt("R_icon");
            R_layout_button = getArguments().getInt("R_layout_button");
            infos = getArguments().getByteArray("Infos");
            builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(titre);
            builder.setIcon(R_icon);
            Mydiagview = getActivity().getLayoutInflater().inflate(R_layout_button, null);
            builder.setView(Mydiagview);
            try {
			((TextView) Mydiagview.findViewById(R.id.dialog_show_login_placeholder))
					.setText(new String(Arrays.copyOfRange(infos, 1, infos[0] + 1), "UTF-8"));
		    } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }

		    try {
			((TextView) Mydiagview.findViewById(R.id.dialog_show_password_placeholder))
					.setText(new String(Arrays.copyOfRange(infos, infos[0]+1, infos.length),"UTF-8"));
		    } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
            Mydiagview.findViewById(R.id.button_close).setOnClickListener(this);
            Mydiagview.findViewById(R.id.button_delete).setOnClickListener(this);
            Mydiagview.findViewById(R.id.button_edit).setOnClickListener(this);
            Mydiagview.findViewById(R.id.dialog_show_login).setOnClickListener(this);
            Mydiagview.findViewById(R.id.dialog_show_password).setOnClickListener(this);
            return builder.create();

        }

        @Override
        public void onClick(View v) {
            ((ListActivity)getActivity()).MyonClick(v);
        }
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		// Récupération du password
		Intent myIntent = getIntent();
		password = myIntent.getCharArrayExtra("password");


		// Instanciation du cipher et de la datasource
		datasource = new PasswordLogDataSource(getApplicationContext());
		cipher = new MyCipher();

		// Gestion de la listView
		listview = (ListView) findViewById(R.id.list);
		refreshList();
		listview.setOnItemClickListener(this);

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		text = cipher.dechiffre(password, values[position].getCrypto());
        laposition = position ;
        InfoDialog = MyDialog.newInstance(values[position].getSiteName(), R.drawable.ic_menu_web,R.layout.dialog_show,text);
        InfoDialog.show(getFragmentManager(), "Informations de connexion");

	}

	/**
	 * Permet de modifier un site dans la base
	 * 
	 * @param index
	 *            L'index du site dans le tableau values[]
	 * @param text
	 *            La chaine login|password décrypté, pour initialiser les champs
	 *            textes
	 */
	public void editSite(final int index, byte[] text) {
		
		byte j;
		byte aux[];
		
		/* pour pouvoir le recuperer dans la relance de la boite d'edition si jamais un champ est vide */
		final byte[] letexte = Arrays.copyOf(text, text.length);
		PasswordLog passwordLog = values[index];
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View addSiteView = inflater.inflate(R.layout.dialog_add, null);
		j = text[0];
		aux = Arrays.copyOfRange(text, 1, j+1);
		try {
			((EditText) addSiteView.findViewById(R.id.dialog_add_et_login))
					.setText(new String(aux, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Arrays.fill(aux, (byte) 0);

		aux = Arrays.copyOfRange(text, j+1, text.length);	
		try {
			((EditText) addSiteView.findViewById(R.id.dialog_add_et_password))
					.setText(new String(aux,"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		Arrays.fill(aux, (byte) 0);

		EditText editSiteName = ((EditText) addSiteView
				.findViewById(R.id.dialog_add_et_name));
		//editSiteName.setFocusable(false);
		//editSiteName.setTextColor(getResources().getColor(R.color.Grey));
		final String OldSiteName = passwordLog.getSiteName();
		editSiteName.setText(OldSiteName);

		builder.setView(addSiteView)
				.setPositiveButton(R.string.validate_label,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								String siteName = ((EditText) addSiteView
										.findViewById(R.id.dialog_add_et_name))
										.getText().toString();
								Editable ELogin = ((EditText) addSiteView
										.findViewById(R.id.dialog_add_et_login))
										.getText();
								Editable EPassword = ((EditText) addSiteView
										.findViewById(R.id.dialog_add_et_password))
										.getText();

								byte[] siteLogin = null;
								byte[] sitePassword = null;
								
								
								try {
									siteLogin = ELogin.toString().getBytes("UTF-8");
								} catch (UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								try {
									sitePassword = EPassword.toString().getBytes("UTF-8");
								} catch (UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								if (!siteName.equals("") && (siteLogin.length != 0) && (sitePassword.length != 0))
								{
									ByteBuffer plainText = ByteBuffer.allocate(siteLogin.length+sitePassword.length+1);
									plainText.put((byte)siteLogin.length);
									plainText.put(siteLogin);
									plainText.put(sitePassword);
									Arrays.fill(siteLogin, (byte)0);
									Arrays.fill(sitePassword, (byte)0);
									
									byte[] crypto = cipher.chiffre(password, plainText.array());
									plainText.rewind();
									plainText.put((byte)0);
									plainText.put(siteLogin,0,siteLogin.length);
									plainText.put(sitePassword,0,sitePassword.length);
									PasswordLog passwordLog = new PasswordLog(siteName, crypto);
									datasource.open();
									datasource.update(OldSiteName,passwordLog);
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
									new AlertDialog.Builder(builder.getContext())
									.setIcon(android.R.drawable.ic_dialog_alert)
									.setTitle(R.string.incomplete_Field)
									.setMessage(Alert_message)
									.setPositiveButton(R.string.validate_label,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog,
														int which) {
													//refreshList();
													editSite(index, letexte);
													Arrays.fill(letexte, (byte) 0);
												}

											}).show();

								}
								refreshList();

							}
						})
				.setNegativeButton(R.string.cancel_label,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		builder.create().show();
		Arrays.fill(text, (byte) 0);
	}

	/**
	 * Supprime de la base le site passé en paramètre
	 * 
	 * @param index
	 *            L'index du site dans le tableau values[]
	 */
	public void deleteSite(int index) {
		// final pour y avoir accès dans le onclick
		final PasswordLog passwordLog = values[index];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.deleteOne_message)
                .setPositiveButton(R.string.validate_label,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                datasource.open();
                                datasource.delete(passwordLog);
                                datasource.close();
                                refreshList();
                            }

                        })
                .setNegativeButton(R.string.cancel_label,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        builder.create().show();
	}

	/**
	 * Récupère les données dans la base et met à jour l'Adapter de la listView
	 */
	public void refreshList() {
		// R'ecup'eration des donn'ees dans la base
		datasource.open();
		values = new PasswordLog[] {};
		values = datasource.getAllFrom(1).toArray(values);
		datasource.close();
		ArrayAdapter<PasswordLog> adapter = new ArrayAdapter<PasswordLog>(this,
				R.layout.list_element, R.id.listview_text, values);
		listview.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

    public void MyonClick(View v)
    {
        // TODO Auto-generated method stub
        String clipkey="", alerte="", ClipString="";

        View parent_v = (View) v.getParent();

        switch (v.getId())
        {
            case R.id.button_close :
                Arrays.fill(text,(byte) 0);
                InfoDialog.dismiss();
                break;
            case R.id.button_delete :
                deleteSite(laposition);
                Arrays.fill(text, (byte) 0);
                InfoDialog.dismiss();
                break;

            case R.id.button_edit :
                editSite(laposition,text);
                Arrays.fill(text, (byte) 0);
                InfoDialog.dismiss();
                break;

            case R.id.dialog_show_login :
                clipkey = "login";
                alerte = "Identifiant copié dans le presse-papier";
                ClipString = ((TextView) parent_v.findViewById(R.id.dialog_show_login_placeholder)).getText().toString();
                break;

            case R.id.dialog_show_password :
                clipkey = "password";
                alerte = "Attention !! mot de passe copié dans le presse-papier";
                ClipString = ((TextView) parent_v.findViewById(R.id.dialog_show_password_placeholder)).getText().toString();
                break;
        }
         if ((v.getId() == R.id.dialog_show_password) || (v.getId() == R.id.dialog_show_login)) {
             ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
             ClipData clip = ClipData.newPlainText(clipkey, ClipString);
             clipboard.setPrimaryClip(clip);
             Toast.makeText(getApplicationContext(), alerte, Toast.LENGTH_SHORT).show();
         }
    }

}
