package fr.toulon.masterdapm.MonCoffre;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class Apropos extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_apropos);
		
		TextView texte = (TextView) findViewById(R.id.Textapropos);
		texte.setText(Html.fromHtml("<body style='text-align:center'>"
				+ "L'application <b>Mon Coffre</b> a &eacute;t&eacute; cr&eacute;&eacute;e &agrave; partir des sources "
				+ "d'un projet d&eacute;velopp&eacute; "
				+ "dans le cadre "
				+ "du module"+
				"<div align=center><em>\"Protocoles cryptographiques\"</em></div> du Master DAPM (D&eacute;veloppement et Applications sur Plateformes Mobiles) de"
				+ " l'universit&eacute; de Toulon. <br><br>"
				+ "Elle permet de stocker dans une base de donn&eacute;es s&eacute;curis&eacute;e, l'ensemble de vos identifiants et mots de passe que vous"
				+ " utilisez pour vous connecter sur diff&eacute;rents sites. Le tout est prot&eacute;g&eacute; par un unique mot de passe qu'il "
				+ "vous suffit de retenir.<br><br>"
				+ "La base de donn&eacute;es est prot&eacute;g&eacute;e en utilisant le standard de chiffrement <a href=http://en.wikipedia.org/wiki/Advanced_Encryption_Standard>AES</a>.<br><br>"
				+"La cl&eacute; secr&egrave;te de 128 bits utilis&eacute;e par l'AES est d&eacute;riv&eacute;e de votre unique mot de passe en utilisant "
				+ "la sp&eacute;cification <a href='https://www.ietf.org/rfc/rfc2898.txt'>PBKDF2</a> &eacute;tabli par le laboratoire RSA."
				+ "</body>"));
		texte.setLinksClickable(true);
		texte.setMovementMethod(LinkMovementMethod.getInstance());
	}
}
