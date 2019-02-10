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

    private MyDialogIm InfoDialog;
	static private PasswordLog[] values;
    static private byte[] text;
    final int action_delete = 1;
    final int action_empty = 2 ;
    static final int action_edit = 3;

    public static class MyDialog extends DialogFragment {

        public MyDialog()
        {

        }

        public static MyDialog newInstance(int title, int message, int mess_positive, int mess_negative, int icon, int rlayout, int idx, int action) {

            MyDialog frag = new MyDialog();
            Bundle args = new Bundle();
            args.putInt("R_title", title);
            args.putInt("R_mess_positive",mess_positive);
            args.putInt("R_mess_negative",mess_negative);
            args.putInt("R_mess",message);
            args.putInt("R_icon",icon);
            args.putInt("idx",idx);
			args.putInt("R_layout",rlayout);
            args.putInt("action",action);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            int title, icon, mess_positive, mess_negative, message, rlayout;
			final int index, action;
			/* final pour pouvoir être passé dans le listener */
            AlertDialog.Builder builder;
            final View Mydiagview;

            title = getArguments().getInt("R_title");
            mess_positive = getArguments().getInt("R_mess_positive");
            mess_negative = getArguments().getInt("R_mess_negative");
            message = getArguments().getInt("R_mess");
            icon = getArguments().getInt("R_icon");
			rlayout = getArguments().getInt("R_layout");
            index = getArguments().getInt("idx");
            action = getArguments().getInt("action");

            builder = new AlertDialog.Builder(getActivity());
			/* la doc Android spécifie que 0 ne paut pas être une valeur d'identifiant */
            if (icon != 0)
                builder.setIcon(icon);
            if (title != 0)
                builder.setTitle(title);
            if (message != 0)
                builder.setMessage(message);
            if (rlayout != 0) {
                Mydiagview = getActivity().getLayoutInflater().inflate(rlayout, null);
                if (action == action_edit){
                    int j;
                    byte[] aux;

                    PasswordLog passwordLog = values[index];

                    j = text[0];
                    aux = Arrays.copyOfRange(text, 1, j+1);
                    try {
                        ((EditText) Mydiagview.findViewById(R.id.dialog_add_et_login))
                                .setText(new String(aux, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Arrays.fill(aux, (byte) 0);

                    aux = Arrays.copyOfRange(text, j+1, text.length);
                    try {
                        ((EditText) Mydiagview.findViewById(R.id.dialog_add_et_password))
                                .setText(new String(aux,"UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Arrays.fill(aux, (byte) 0);
                    EditText editSiteName = ((EditText) Mydiagview
                            .findViewById(R.id.dialog_add_et_name));
                    final String OldSiteName = passwordLog.getSiteName();
                    editSiteName.setText(OldSiteName);
                }
                builder.setView(Mydiagview);
            }
            else
            Mydiagview = null;
            builder.setPositiveButton(mess_positive,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((ListActivity)getActivity()).doPositiveClick(action,index,Mydiagview);
                                }
                            }
                    );
            if (mess_negative != 0)
                    builder.setNegativeButton(mess_negative,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((ListActivity)getActivity()).doNegativeClick(action,dialog);
                                }
                            }
                    );
            return builder.create();
        }
    }


    public static class MyDialogIm extends DialogFragment implements OnClickListener{
        /* classe pour boite de dialogue personnalisée avec images à la place des boutons */

        public MyDialogIm()
        {
        }

        public static MyDialogIm newInstance(String letitre, int icon, int layout_button, byte[] infos_connexion) {
            MyDialogIm frag = new MyDialogIm();
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
        InfoDialog = MyDialogIm.newInstance(values[position].getSiteName(), R.drawable.ic_menu_web,R.layout.dialog_show,text);
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
	public void editSite(final int index, final byte[] text) {
		
        MyDialog InfoEditSite = MyDialog.newInstance(0,0,R.string.validate_label,R.string.cancel_label,0,R.layout.dialog_add,index,action_edit);
        InfoEditSite.show(getFragmentManager(), "");
	}

	/**
	 * Supprime de la base le site passé en paramètre
	 * 
	 * @param index
	 *            L'index du site dans le tableau values[]
	 */

    public void deleteSite(int index) {

        MyDialog Info = MyDialog.newInstance(0,R.string.deleteOne_message,R.string.validate_label,R.string.cancel_label,0,0,index,action_delete);
        Info.show(getFragmentManager(), "");
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

    public void doPositiveClick(int action, int index, View v)
    {
        switch (action)
        {
            case action_delete :
                datasource.open();
                datasource.delete(values[index]);
                datasource.close();
                refreshList();
                break;
            case action_empty :
                editSite(index,text);
                break;
            case action_edit :
                String siteName = ((EditText) v
                        .findViewById(R.id.dialog_add_et_name))
                        .getText().toString();
                Editable ELogin = ((EditText) v
                        .findViewById(R.id.dialog_add_et_login))
                        .getText();
                Editable EPassword = ((EditText) v
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
                    Arrays.fill(text, (byte) 0);

                    byte[] crypto = cipher.chiffre(password, plainText.array());
                    plainText.rewind();
                    plainText.put((byte)0);
                    plainText.put(siteLogin,0,siteLogin.length);
                    plainText.put(sitePassword,0,sitePassword.length);
                    PasswordLog passwordLog = new PasswordLog(siteName, crypto);
                    datasource.open();
                    datasource.update(values[index].getSiteName(),passwordLog);
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
                    MyDialog Infoempty = MyDialog.newInstance(R.string.incomplete_Field,Alert_message,R.string.validate_label,0,android.R.drawable.ic_dialog_alert,0,index,action_empty);
                    Infoempty.show(getFragmentManager(), "");
                }
                refreshList();
                break;
        }
    }

    public void doNegativeClick(int action, DialogInterface d)
    {
        Arrays.fill(text,(byte)0);
        d.dismiss();
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
