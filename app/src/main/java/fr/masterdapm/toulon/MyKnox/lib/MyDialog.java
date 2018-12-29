package fr.masterdapm.toulon.MyKnox.lib;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

public class MyDialog extends DialogFragment {
	
	String titre;
	View showView ;
    int R_icon;
    int default_view = 1;
    AlertDialog.Builder builder ;

	public MyDialog()
	{

	}

	public MyDialog(View v, String letitre, int icon)
	{
		titre = letitre;
		showView = v;
        R_icon =icon ;
        default_view = 0;
	}

    public MyDialog(String letitre, int icon)
    {
        titre = letitre;
        R_icon =icon ;
    }

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		

		builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(titre);
		builder.setIcon(R_icon);
		if (default_view != 1) builder.setView(showView);
		return builder.create();
		
	}

    public AlertDialog.Builder getBuilder(){
        return builder;
    }
}
