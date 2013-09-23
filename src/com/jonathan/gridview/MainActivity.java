package com.jonathan.gridview;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.example.gridview.R;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.GridView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private NfcAdapter mNfcAdapter;
	public static final String MIME_TEXT_PLAIN = "text/plain";
	private static final String TAG = "nfcdemo";
	ImageAdapter imageAdapter;

	// public ArrayList pokemonCaptured;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		GridView gridview = (GridView) findViewById(R.id.gridview);
		imageAdapter = new ImageAdapter(this);
		// gridview.setAdapter(new ImageAdapter(this));
		gridview.setAdapter(imageAdapter);
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		handleIntent(getIntent());

	}

	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setupForegroundDispatch(this, mNfcAdapter);
	}

	@Override
	protected void onPause() {
		stopForegroundDispatch(this, mNfcAdapter);
		super.onPause();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	public static void setupForegroundDispatch(final Activity activity,
			NfcAdapter adapter) {
		final Intent intent = new Intent(activity.getApplicationContext(),
				activity.getClass());
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		final PendingIntent pendingIntent = PendingIntent.getActivity(
				activity.getApplicationContext(), 0, intent, 0);
		IntentFilter[] filters = new IntentFilter[1];
		String[][] techList = new String[][] {};
		filters[0] = new IntentFilter();
		filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
		filters[0].addCategory(Intent.CATEGORY_DEFAULT);
		try {
			filters[0].addDataType(MIME_TEXT_PLAIN);
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("check your mine type");
		}
		adapter.enableForegroundDispatch(activity, pendingIntent, filters,
				techList);
	}

	public static void stopForegroundDispatch(final Activity activity,
			NfcAdapter adapter) {
		adapter.disableForegroundDispatch(activity);
	}

	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			String type = intent.getType();
			if (MIME_TEXT_PLAIN.equals(type)) {
				Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
				new NdefReaderTask().execute(tag);
			} else {
				Log.d(TAG, "Wrong mime type:" + type);
			}

		} else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
			// use tech discovered intent
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String[] techList = tag.getTechList();
			String searchedTech = Ndef.class.getName();
			for (String tech : techList) {
				if (searchedTech.equals(tech)) {
					new NdefReaderTask().execute(tag);
					break;
				}
			}
		}
	}

	private class NdefReaderTask extends AsyncTask<Tag, Void, String> {
		@Override
		protected String doInBackground(Tag... params) {
			Tag tag = params[0];
			Ndef ndef = Ndef.get(tag);
			if (ndef == null) {
				// somethings broken
				return null;
			}
			NdefMessage ndefMessage = ndef.getCachedNdefMessage();
			String message = ndefMessage.toString();
			Log.d("A", "MESSAGE_12345=" + message);
			// mTextView.setText(message);
			NdefRecord[] records = ndefMessage.getRecords();
			for (NdefRecord ndefRecord : records) {
				if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN
						&& Arrays.equals(ndefRecord.getType(),
								NdefRecord.RTD_TEXT)) {
					try {
						return readText(ndefRecord);
					} catch (UnsupportedEncodingException e) {
						// Log.e(TAG, "unsupported encoding", e);
					}
				}
			}
			return null;
		}

		private String readText(NdefRecord record)
				throws UnsupportedEncodingException {
			/*
			 * bit 7 is encoding 6 mustb e 0 5...0 is iana language code
			 */
			byte[] payload = record.getPayload();

			// get the text encoding
			String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8"
					: "UTF-16";

			// get language code now
			int languageCodeLength = payload[0] & 0077;

			// Log.d("A","toMimeType_12345=" + record.toMimeType());

			return new String(payload, languageCodeLength + 1, payload.length
					- languageCodeLength - 1, textEncoding);
			// http://en.wikipedia.org/wiki/List_of_Pok%C3%A9mon
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				Log.d("A", "RESULT_12345=" + result);
				SharedPreferences sharedPreferences = PreferenceManager
						.getDefaultSharedPreferences(getApplication());
				Editor editor = sharedPreferences.edit();
				editor.putString(result, result);
				editor.commit();
				Toast.makeText(getApplication(), "You caputured " + result,
						Toast.LENGTH_LONG).show();
				if (result.equals("bulbasaur")
						|| sharedPreferences.getString("bulbasaur", "no")
								.equals("bulbasaur")) {
					imageAdapter.pokemonCaptured(0, R.drawable.bulbasaur);
				}
				if (result.equals("ivysaur")
						|| sharedPreferences.getString("ivysaur", "no").equals(
								"ivysaur")) {
					imageAdapter.pokemonCaptured(1, R.drawable.ivysaur);
				}
				if (result.equals("venusaur")
						|| sharedPreferences.getString("venusaur", "").equals(
								"venusaur")) {
					imageAdapter.pokemonCaptured(2, R.drawable.venusaur);
				}
				if (result.equals("charmander")
						|| sharedPreferences.getString("charmander", "")
								.equals("charmander")) {
					imageAdapter.pokemonCaptured(3, R.drawable.charmander);
				}
				if (result.equals("charmeleon")
						|| sharedPreferences.getString("charmeleon", "")
								.equals("charmeleon")) {
					imageAdapter.pokemonCaptured(4, R.drawable.charmeleon);
				}
				if (result.equals("charizard")
						|| sharedPreferences.getString("charizard", "").equals(
								"charizard")) {
					imageAdapter.pokemonCaptured(5, R.drawable.charizard);
				}
				if (result.equals("squirtle")
						|| sharedPreferences.getString("squirtle", "").equals(
								"squirtle")) {
					imageAdapter.pokemonCaptured(6, R.drawable.squirtle);
				}
				if (result.equals("wartortle")
						|| sharedPreferences.getString("wartortle", "").equals(
								"wartortle")) {
					imageAdapter.pokemonCaptured(7, R.drawable.wartortle);
				}
				if (result.equals("blastoise")
						|| sharedPreferences.getString("blastoise", "").equals(
								"blastoise")) {
					imageAdapter.pokemonCaptured(8, R.drawable.blastoise);
				}
				if (result.equals("caterpie")
						|| sharedPreferences.getString("caterpie", "").equals(
								"caterpie")) {
					imageAdapter.pokemonCaptured(9, R.drawable.caterpie);
				}
				if (result.equals("metapod")
						|| sharedPreferences.getString("metapod", "").equals(
								"metapod")) {
					imageAdapter.pokemonCaptured(10, R.drawable.metapod);
				}
				if (result.equals("butterfree")
						|| sharedPreferences.getString("butterfree", "")
								.equals("butterfree")) {
					imageAdapter.pokemonCaptured(11, R.drawable.butterfree);
				}
				if (result.equals("weedle")
						|| sharedPreferences.getString("weedle", "").equals(
								"weedle")) {
					imageAdapter.pokemonCaptured(12, R.drawable.weedle);
				}
				if (result.equals("kakuna")
						|| sharedPreferences.getString("kakuna", "").equals(
								"kakuna")) {
					imageAdapter.pokemonCaptured(13, R.drawable.kakuna);
				}
				if (result.equals("beedrill")
						|| sharedPreferences.getString("beedrill", "").equals(
								"beedrill")) {
					imageAdapter.pokemonCaptured(14, R.drawable.beedrill);
				}
				if (result.equals("pidgey")
						|| sharedPreferences.getString("pidgey", "").equals(
								"pidgey")) {
					imageAdapter.pokemonCaptured(15, R.drawable.pidgey);
				}
				if (result.equals("pidgeotto")
						|| sharedPreferences.getString("pidgeotto", "").equals(
								"pidgeotto")) {
					imageAdapter.pokemonCaptured(16, R.drawable.pidgeotto);
				}
				if (result.equals("pidgeot")
						|| sharedPreferences.getString("pidgeot", "").equals(
								"pidgeot")) {
					imageAdapter.pokemonCaptured(17, R.drawable.pidgeot);
				}
				if (result.equals("rattata")
						|| sharedPreferences.getString("rattata", "").equals(
								"rattata")) {
					imageAdapter.pokemonCaptured(18, R.drawable.rattata);
				}
				if (result.equals("raticate")
						|| sharedPreferences.getString("raticate", "").equals(
								"raticate")) {
					imageAdapter.pokemonCaptured(19, R.drawable.raticate);
				}
				if (result.equals("spearow")
						|| sharedPreferences.getString("spearow", "").equals(
								"spearow")) {
					imageAdapter.pokemonCaptured(20, R.drawable.spearow);
				}
				if (result.equals("fearow")
						|| sharedPreferences.getString("fearow", "").equals(
								"fearow")) {
					imageAdapter.pokemonCaptured(21, R.drawable.fearow);
				}
				if (result.equals("ekans")
						|| sharedPreferences.getString("ekans", "").equals(
								"ekans")) {
					imageAdapter.pokemonCaptured(22, R.drawable.ekans);
				}
				if (result.equals("arbok")
						|| sharedPreferences.getString("arbok", "").equals(
								"arbok")) {
					imageAdapter.pokemonCaptured(23, R.drawable.arbok);
				}
				if (result.equals("pikachu")
						|| sharedPreferences.getString("pikachu", "").equals(
								"pikachu")) {
					imageAdapter.pokemonCaptured(24, R.drawable.pikachu);
				}
				if (result.equals("raichu")
						|| sharedPreferences.getString("raichu", "").equals(
								"raichu")) {
					imageAdapter.pokemonCaptured(25, R.drawable.raichu);
				}
				if (result.equals("sandshrew")
						|| sharedPreferences.getString("sandshrew", "").equals(
								"sandshrew")) {
					imageAdapter.pokemonCaptured(26, R.drawable.sandshrew);
				}
				if (result.equals("sandslash")
						|| sharedPreferences.getString("sandslash", "").equals(
								"sandslash")) {
					imageAdapter.pokemonCaptured(27, R.drawable.sandslash);
				}
				if (result.equals("nidoranf")
						|| sharedPreferences.getString("nidoranf", "").equals(
								"nidoranf")) {
					imageAdapter.pokemonCaptured(28, R.drawable.nidoranf);
				}
				if (result.equals("nidorina")
						|| sharedPreferences.getString("nidorina", "").equals(
								"nidorina")) {
					imageAdapter.pokemonCaptured(29, R.drawable.nidorina);
				}
				if (result.equals("nidoqueen")
						|| sharedPreferences.getString("nidoqueen", "").equals(
								"nidoqueen")) {
					imageAdapter.pokemonCaptured(30, R.drawable.nidoqueen);
				}
				if (result.equals("nidoranm")
						|| sharedPreferences.getString("nidoranm", "").equals(
								"nidoranm")) {
					imageAdapter.pokemonCaptured(31, R.drawable.nidoranm);
				}
				if (result.equals("nidorino")
						|| sharedPreferences.getString("nidorino", "").equals(
								"nidorino")) {
					imageAdapter.pokemonCaptured(32, R.drawable.nidorino);
				}
				if (result.equals("nidoking")
						|| sharedPreferences.getString("nidoking", "").equals(
								"nidoking")) {
					imageAdapter.pokemonCaptured(33, R.drawable.nidoking);
				}
				if (result.equals("clefairy")
						|| sharedPreferences.getString("clefairy", "").equals(
								"clefairy")) {
					imageAdapter.pokemonCaptured(34, R.drawable.clefairy);
				}
				if (result.equals("clefable")
						|| sharedPreferences.getString("clefable", "").equals(
								"clefable")) {
					imageAdapter.pokemonCaptured(35, R.drawable.clefable);
				}
				if (result.equals("vulpix")
						|| sharedPreferences.getString("vulpix", "").equals(
								"vulpix")) {
					imageAdapter.pokemonCaptured(36, R.drawable.vulpix);
				}
				if (result.equals("ninetales")
						|| sharedPreferences.getString("ninetales", "").equals(
								"ninetales")) {
					imageAdapter.pokemonCaptured(37, R.drawable.ninetales);
				}
				if (result.equals("jigglypuff")
						|| sharedPreferences.getString("jigglypuff", "")
								.equals("jigglypuff")) {
					imageAdapter.pokemonCaptured(38, R.drawable.jigglypuff);
				}
				if (result.equals("wigglytuff")
						|| sharedPreferences.getString("wigglytuff", "")
								.equals("wigglytuff")) {
					imageAdapter.pokemonCaptured(39, R.drawable.wigglytuff);
				}
				if (result.equals("zubat")
						|| sharedPreferences.getString("zubat", "").equals(
								"zubat")) {
					imageAdapter.pokemonCaptured(40, R.drawable.zubat);
				}
				if (result.equals("golbat")
						|| sharedPreferences.getString("golbat", "").equals(
								"golbat")) {
					imageAdapter.pokemonCaptured(41, R.drawable.golbat);
				}
				if (result.equals("oddish")
						|| sharedPreferences.getString("oddish", "").equals(
								"oddish")) {
					imageAdapter.pokemonCaptured(42, R.drawable.oddish);
				}
				if (result.equals("gloom")
						|| sharedPreferences.getString("gloom", "").equals(
								"gloom")) {
					imageAdapter.pokemonCaptured(43, R.drawable.gloom);
				}
				if (result.equals("vileplume")
						|| sharedPreferences.getString("vileplume", "").equals(
								"vileplume")) {
					imageAdapter.pokemonCaptured(44, R.drawable.vileplume);
				}
				if (result.equals("paras")
						|| sharedPreferences.getString("paras", "").equals(
								"paras")) {
					imageAdapter.pokemonCaptured(45, R.drawable.paras);
				}
				if (result.equals("parasect")
						|| sharedPreferences.getString("parasect", "").equals(
								"parasect")) {
					imageAdapter.pokemonCaptured(46, R.drawable.parasect);
				}
				if (result.equals("venonat")
						|| sharedPreferences.getString("venonat", "").equals(
								"venonat")) {
					imageAdapter.pokemonCaptured(47, R.drawable.venonat);
				}
				if (result.equals("venomoth")
						|| sharedPreferences.getString("venomoth", "").equals(
								"venomoth")) {
					imageAdapter.pokemonCaptured(48, R.drawable.venomoth);
				}
				if (result.equals("diglett")
						|| sharedPreferences.getString("diglett", "").equals(
								"diglett")) {
					imageAdapter.pokemonCaptured(49, R.drawable.diglett);
				}
				if (result.equals("dugtrio")
						|| sharedPreferences.getString("dugtrio", "").equals(
								"dugtrio")) {
					imageAdapter.pokemonCaptured(50, R.drawable.dugtrio);
				}
				if (result.equals("meowth")
						|| sharedPreferences.getString("meowth", "").equals(
								"meowth")) {
					imageAdapter.pokemonCaptured(51, R.drawable.meowth);
				}
				if (result.equals("persian")
						|| sharedPreferences.getString("persian", "").equals(
								"persian")) {
					imageAdapter.pokemonCaptured(52, R.drawable.persian);
				}
				if (result.equals("psyduck")
						|| sharedPreferences.getString("psyduck", "").equals(
								"psyduck")) {
					imageAdapter.pokemonCaptured(53, R.drawable.psyduck);
				}
				if (result.equals("golduck")
						|| sharedPreferences.getString("golduck", "").equals(
								"golduck")) {
					imageAdapter.pokemonCaptured(54, R.drawable.golduck);
				}
				if (result.equals("mankey")
						|| sharedPreferences.getString("mankey", "").equals(
								"mankey")) {
					imageAdapter.pokemonCaptured(55, R.drawable.mankey);
				}
				if (result.equals("primeape")
						|| sharedPreferences.getString("primeape", "").equals(
								"primeape")) {
					imageAdapter.pokemonCaptured(56, R.drawable.primeape);
				}
				if (result.equals("growlithe")
						|| sharedPreferences.getString("growlithe", "").equals(
								"growlithe")) {
					imageAdapter.pokemonCaptured(57, R.drawable.growlithe);
				}
				if (result.equals("arcanine")
						|| sharedPreferences.getString("arcanine", "").equals(
								"arcanine")) {
					imageAdapter.pokemonCaptured(58, R.drawable.arcanine);
				}
				if (result.equals("poliwag")
						|| sharedPreferences.getString("poliwag", "").equals(
								"poliwag")) {
					imageAdapter.pokemonCaptured(59, R.drawable.poliwag);
				}
				if (result.equals("poliwhirl")
						|| sharedPreferences.getString("poliwhirl", "").equals(
								"poliwhirl")) {
					imageAdapter.pokemonCaptured(60, R.drawable.poliwhirl);
				}
				if (result.equals("poliwrath")
						|| sharedPreferences.getString("poliwrath", "").equals(
								"poliwrath")) {
					imageAdapter.pokemonCaptured(61, R.drawable.poliwrath);
				}
				if (result.equals("abra")
						|| sharedPreferences.getString("abra", "").equals(
								"abra")) {
					imageAdapter.pokemonCaptured(62, R.drawable.abra);
				}
				if (result.equals("kadabra")
						|| sharedPreferences.getString("kadabra", "").equals(
								"kadabra")) {
					imageAdapter.pokemonCaptured(63, R.drawable.kadabra);
				}
				if (result.equals("alakazam")
						|| sharedPreferences.getString("alakazam", "").equals(
								"alakazam")) {
					imageAdapter.pokemonCaptured(64, R.drawable.alakazam);
				}
				if (result.equals("machop")
						|| sharedPreferences.getString("machop", "").equals(
								"machop")) {
					imageAdapter.pokemonCaptured(65, R.drawable.machop);
				}
				if (result.equals("machoke")
						|| sharedPreferences.getString("machoke", "").equals(
								"machoke")) {
					imageAdapter.pokemonCaptured(66, R.drawable.machoke);
				}
				if (result.equals("machamp")
						|| sharedPreferences.getString("machamp", "").equals(
								"machamp")) {
					imageAdapter.pokemonCaptured(67, R.drawable.machamp);
				}
				if (result.equals("bellsprout")
						|| sharedPreferences.getString("bellsprout", "")
								.equals("bellsprout")) {
					imageAdapter.pokemonCaptured(68, R.drawable.bellsprout);
				}
				if (result.equals("weepinbell")
						|| sharedPreferences.getString("weepinbell", "")
								.equals("weepinbell")) {
					imageAdapter.pokemonCaptured(69, R.drawable.weepinbell);
				}
				if (result.equals("victreebel")
						|| sharedPreferences.getString("victreebel", "")
								.equals("victreebel")) {
					imageAdapter.pokemonCaptured(70, R.drawable.victreebel);
				}
				if (result.equals("tentacool")
						|| sharedPreferences.getString("tentacool", "").equals(
								"tentacool")) {
					imageAdapter.pokemonCaptured(71, R.drawable.tentacool);
				}
				if (result.equals("tentacruel")
						|| sharedPreferences.getString("tentacruel", "")
								.equals("tentacruel")) {
					imageAdapter.pokemonCaptured(72, R.drawable.tentacruel);
				}
				if (result.equals("geodude")
						|| sharedPreferences.getString("geodude", "").equals(
								"geodude")) {
					imageAdapter.pokemonCaptured(73, R.drawable.geodude);
				}
				if (result.equals("graveler")
						|| sharedPreferences.getString("graveler", "").equals(
								"graveler")) {
					imageAdapter.pokemonCaptured(74, R.drawable.graveler);
				}
				if (result.equals("golem")
						|| sharedPreferences.getString("golem", "").equals(
								"golem")) {
					imageAdapter.pokemonCaptured(75, R.drawable.golem);
				}
				if (result.equals("ponyta")
						|| sharedPreferences.getString("ponyta", "").equals(
								"ponyta")) {
					imageAdapter.pokemonCaptured(76, R.drawable.ponyta);
				}
				if (result.equals("rapidash")
						|| sharedPreferences.getString("rapidash", "").equals(
								"rapidash")) {
					imageAdapter.pokemonCaptured(77, R.drawable.rapidash);
				}
				if (result.equals("slowpoke")
						|| sharedPreferences.getString("slowpoke", "").equals(
								"slowpoke")) {
					imageAdapter.pokemonCaptured(78, R.drawable.slowpoke);
				}
				if (result.equals("slowbro")
						|| sharedPreferences.getString("slowbro", "").equals(
								"slowbro")) {
					imageAdapter.pokemonCaptured(79, R.drawable.slowbro);
				}
				if (result.equals("magnemite")
						|| sharedPreferences.getString("magnemite", "").equals(
								"magnemite")) {
					imageAdapter.pokemonCaptured(80, R.drawable.magnemite);
				}
				if (result.equals("magneton")
						|| sharedPreferences.getString("magneton", "").equals(
								"magneton")) {
					imageAdapter.pokemonCaptured(81, R.drawable.magneton);
				}
				if (result.equals("farfetchd")
						|| sharedPreferences.getString("farfetchd", "").equals(
								"farfetchd")) {
					imageAdapter.pokemonCaptured(82, R.drawable.farfetchd);
				}
				if (result.equals("doduo")
						|| sharedPreferences.getString("doduo", "").equals(
								"doduo")) {
					imageAdapter.pokemonCaptured(83, R.drawable.doduo);
				}
				if (result.equals("dodrio")
						|| sharedPreferences.getString("dodrio", "").equals(
								"dodrio")) {
					imageAdapter.pokemonCaptured(84, R.drawable.dodrio);
				}
				if (result.equals("seel")
						|| sharedPreferences.getString("seel", "").equals(
								"seel")) {
					imageAdapter.pokemonCaptured(85, R.drawable.seel);
				}
				if (result.equals("dewgong")
						|| sharedPreferences.getString("dewgong", "").equals(
								"dewgong")) {
					imageAdapter.pokemonCaptured(86, R.drawable.dewgong);
				}
				if (result.equals("grimer")
						|| sharedPreferences.getString("grimer", "").equals(
								"grimer")) {
					imageAdapter.pokemonCaptured(87, R.drawable.grimer);
				}
				if (result.equals("muk")
						|| sharedPreferences.getString("muk", "").equals("muk")) {
					imageAdapter.pokemonCaptured(88, R.drawable.muk);
				}
				if (result.equals("shellder")
						|| sharedPreferences.getString("shellder", "").equals(
								"shellder")) {
					imageAdapter.pokemonCaptured(89, R.drawable.shellder);
				}
				if (result.equals("cloyster")
						|| sharedPreferences.getString("cloyster", "").equals(
								"cloyster")) {
					imageAdapter.pokemonCaptured(90, R.drawable.cloyster);
				}
				if (result.equals("gastly")
						|| sharedPreferences.getString("gastly", "").equals(
								"gastly")) {
					imageAdapter.pokemonCaptured(91, R.drawable.gastly);
				}
				if (result.equals("haunter")
						|| sharedPreferences.getString("haunter", "").equals(
								"haunter")) {
					imageAdapter.pokemonCaptured(92, R.drawable.haunter);
				}
				if (result.equals("gengar")
						|| sharedPreferences.getString("gengar", "").equals(
								"gengar")) {
					imageAdapter.pokemonCaptured(93, R.drawable.gengar);
				}
				if (result.equals("onix")
						|| sharedPreferences.getString("onix", "").equals(
								"onix")) {
					imageAdapter.pokemonCaptured(94, R.drawable.onix);
				}
				if (result.equals("drowzee")
						|| sharedPreferences.getString("drowzee", "").equals(
								"drowzee")) {
					imageAdapter.pokemonCaptured(95, R.drawable.drowzee);
				}
				if (result.equals("hypno")
						|| sharedPreferences.getString("hypno", "").equals(
								"hypno")) {
					imageAdapter.pokemonCaptured(96, R.drawable.hypno);
				}
				if (result.equals("krabby")
						|| sharedPreferences.getString("krabby", "").equals(
								"krabby")) {
					imageAdapter.pokemonCaptured(97, R.drawable.krabby);
				}
				if (result.equals("kingler")
						|| sharedPreferences.getString("kingler", "").equals(
								"kingler")) {
					imageAdapter.pokemonCaptured(98, R.drawable.kingler);
				}
				if (result.equals("voltorb")
						|| sharedPreferences.getString("voltorb", "").equals(
								"voltorb")) {
					imageAdapter.pokemonCaptured(99, R.drawable.voltorb);
				}
				if (result.equals("electrode")
						|| sharedPreferences.getString("electrode", "").equals(
								"electrode")) {
					imageAdapter.pokemonCaptured(100, R.drawable.electrode);
				}
				if (result.equals("exeggcute")
						|| sharedPreferences.getString("exeggcute", "").equals(
								"exeggcute")) {
					imageAdapter.pokemonCaptured(101, R.drawable.exeggcute);
				}
				if (result.equals("exeggutor")
						|| sharedPreferences.getString("exeggutor", "").equals(
								"exeggutor")) {
					imageAdapter.pokemonCaptured(102, R.drawable.exeggutor);
				}
				if (result.equals("cubone")
						|| sharedPreferences.getString("cubone", "").equals(
								"cubone")) {
					imageAdapter.pokemonCaptured(103, R.drawable.cubone);
				}
				if (result.equals("marowak")
						|| sharedPreferences.getString("marowak", "").equals(
								"marowak")) {
					imageAdapter.pokemonCaptured(104, R.drawable.marowak);
				}
				if (result.equals("hitmonlee")
						|| sharedPreferences.getString("hitmonlee", "").equals(
								"hitmonlee")) {
					imageAdapter.pokemonCaptured(105, R.drawable.hitmonlee);
				}
				if (result.equals("hitmonchan")
						|| sharedPreferences.getString("hitmonchan", "")
								.equals("hitmonchan")) {
					imageAdapter.pokemonCaptured(106, R.drawable.hitmonchan);
				}
				if (result.equals("lickitung")
						|| sharedPreferences.getString("lickitung", "").equals(
								"lickitung")) {
					imageAdapter.pokemonCaptured(107, R.drawable.lickitung);
				}
				if (result.equals("koffing")
						|| sharedPreferences.getString("koffing", "").equals(
								"koffing")) {
					imageAdapter.pokemonCaptured(108, R.drawable.koffing);
				}
				if (result.equals("weezing")
						|| sharedPreferences.getString("weezing", "").equals(
								"weezing")) {
					imageAdapter.pokemonCaptured(109, R.drawable.weezing);
				}
				if (result.equals("rhyhorn")
						|| sharedPreferences.getString("rhyhorn", "").equals(
								"rhyhorn")) {
					imageAdapter.pokemonCaptured(110, R.drawable.rhyhorn);
				}
				if (result.equals("rhydon")
						|| sharedPreferences.getString("rhydon", "").equals(
								"rhydon")) {
					imageAdapter.pokemonCaptured(111, R.drawable.rhydon);
				}
				if (result.equals("chansey")
						|| sharedPreferences.getString("chansey", "").equals(
								"chansey")) {
					imageAdapter.pokemonCaptured(112, R.drawable.chansey);
				}
				if (result.equals("tangela")
						|| sharedPreferences.getString("tangela", "").equals(
								"tangela")) {
					imageAdapter.pokemonCaptured(113, R.drawable.tangela);
				}
				if (result.equals("kangaskhan")
						|| sharedPreferences.getString("kangaskhan", "")
								.equals("kangaskhan")) {
					imageAdapter.pokemonCaptured(114, R.drawable.kangaskhan);
				}
				if (result.equals("horsea")
						|| sharedPreferences.getString("horsea", "").equals(
								"horsea")) {
					imageAdapter.pokemonCaptured(115, R.drawable.horsea);
				}
				if (result.equals("seadra")
						|| sharedPreferences.getString("seadra", "").equals(
								"seadra")) {
					imageAdapter.pokemonCaptured(116, R.drawable.seadra);
				}
				if (result.equals("goldeen")
						|| sharedPreferences.getString("goldeen", "").equals(
								"goldeen")) {
					imageAdapter.pokemonCaptured(117, R.drawable.goldeen);
				}
				if (result.equals("seaking")
						|| sharedPreferences.getString("seaking", "").equals(
								"seaking")) {
					imageAdapter.pokemonCaptured(118, R.drawable.seaking);
				}
				if (result.equals("staryu")
						|| sharedPreferences.getString("staryu", "").equals(
								"staryu")) {
					imageAdapter.pokemonCaptured(119, R.drawable.staryu);
				}
				if (result.equals("starmie")
						|| sharedPreferences.getString("starmie", "").equals(
								"starmie")) {
					imageAdapter.pokemonCaptured(120, R.drawable.starmie);
				}
				if (result.equals("mrmime")
						|| sharedPreferences.getString("mrmime", "").equals(
								"mrmime")) {
					imageAdapter.pokemonCaptured(121, R.drawable.mrmime);
				}
				if (result.equals("scyther")
						|| sharedPreferences.getString("scyther", "").equals(
								"scyther")) {
					imageAdapter.pokemonCaptured(122, R.drawable.scyther);
				}
				if (result.equals("jynx")
						|| sharedPreferences.getString("jynx", "").equals(
								"jynx")) {
					imageAdapter.pokemonCaptured(123, R.drawable.jynx);
				}
				if (result.equals("electabuzz")
						|| sharedPreferences.getString("electabuzz", "")
								.equals("electabuzz")) {
					imageAdapter.pokemonCaptured(124, R.drawable.electabuzz);
				}
				if (result.equals("magmar")
						|| sharedPreferences.getString("magmar", "").equals(
								"magmar")) {
					imageAdapter.pokemonCaptured(125, R.drawable.magmar);
				}
				if (result.equals("pinsir")
						|| sharedPreferences.getString("pinsir", "").equals(
								"pinsir")) {
					imageAdapter.pokemonCaptured(126, R.drawable.pinsir);
				}
				if (result.equals("tauros")
						|| sharedPreferences.getString("tauros", "").equals(
								"tauros")) {
					imageAdapter.pokemonCaptured(127, R.drawable.tauros);
				}
				if (result.equals("magikarp")
						|| sharedPreferences.getString("magikarp", "").equals(
								"magikarp")) {
					imageAdapter.pokemonCaptured(128, R.drawable.magikarp);
				}
				if (result.equals("gyarados")
						|| sharedPreferences.getString("gyarados", "").equals(
								"gyarados")) {
					imageAdapter.pokemonCaptured(129, R.drawable.gyarados);
				}
				if (result.equals("lapras")
						|| sharedPreferences.getString("lapras", "").equals(
								"lapras")) {
					imageAdapter.pokemonCaptured(130, R.drawable.lapras);
				}
				if (result.equals("ditto")
						|| sharedPreferences.getString("ditto", "").equals(
								"ditto")) {
					imageAdapter.pokemonCaptured(131, R.drawable.ditto);
				}
				if (result.equals("eevee")
						|| sharedPreferences.getString("eevee", "").equals(
								"eevee")) {
					imageAdapter.pokemonCaptured(132, R.drawable.eevee);
				}
				if (result.equals("vaporeon")
						|| sharedPreferences.getString("vaporeon", "").equals(
								"vaporeon")) {
					imageAdapter.pokemonCaptured(133, R.drawable.vaporeon);
				}
				if (result.equals("jolteon")
						|| sharedPreferences.getString("jolteon", "").equals(
								"jolteon")) {
					imageAdapter.pokemonCaptured(134, R.drawable.jolteon);
				}
				if (result.equals("flareon")
						|| sharedPreferences.getString("flareon", "").equals(
								"flareon")) {
					imageAdapter.pokemonCaptured(135, R.drawable.flareon);
				}
				if (result.equals("porygon")
						|| sharedPreferences.getString("porygon", "").equals(
								"porygon")) {
					imageAdapter.pokemonCaptured(136, R.drawable.porygon);
				}
				if (result.equals("omanyte")
						|| sharedPreferences.getString("omanyte", "").equals(
								"omanyte")) {
					imageAdapter.pokemonCaptured(137, R.drawable.omanyte);
				}
				if (result.equals("omastar")
						|| sharedPreferences.getString("omastar", "").equals(
								"omastar")) {
					imageAdapter.pokemonCaptured(138, R.drawable.omastar);
				}
				if (result.equals("kabuto")
						|| sharedPreferences.getString("kabuto", "").equals(
								"kabuto")) {
					imageAdapter.pokemonCaptured(139, R.drawable.kabuto);
				}
				if (result.equals("kabutops")
						|| sharedPreferences.getString("kabutops", "").equals(
								"kabutops")) {
					imageAdapter.pokemonCaptured(140, R.drawable.kabutops);
				}
				if (result.equals("aerodactyl")
						|| sharedPreferences.getString("aerodactyl", "")
								.equals("aerodactyl")) {
					imageAdapter.pokemonCaptured(141, R.drawable.aerodactyl);
				}
				if (result.equals("snorlax")
						|| sharedPreferences.getString("snorlax", "").equals(
								"snorlax")) {
					imageAdapter.pokemonCaptured(142, R.drawable.snorlax);
				}
				if (result.equals("articuno")
						|| sharedPreferences.getString("articuno", "").equals(
								"articuno")) {
					imageAdapter.pokemonCaptured(143, R.drawable.articuno);
				}
				if (result.equals("zapdos")
						|| sharedPreferences.getString("zapdos", "").equals(
								"zapdos")) {
					imageAdapter.pokemonCaptured(144, R.drawable.zapdos);
				}
				if (result.equals("moltres")
						|| sharedPreferences.getString("moltres", "").equals(
								"moltres")) {
					imageAdapter.pokemonCaptured(145, R.drawable.moltres);
				}
				if (result.equals("dratini")
						|| sharedPreferences.getString("dratini", "").equals(
								"dratini")) {
					imageAdapter.pokemonCaptured(146, R.drawable.dratini);
				}
				if (result.equals("dragonair")
						|| sharedPreferences.getString("dragonair", "").equals(
								"dragonair")) {
					imageAdapter.pokemonCaptured(147, R.drawable.dragonair);
				}
				if (result.equals("dragonite")
						|| sharedPreferences.getString("dragonite", "").equals(
								"dragonite")) {
					imageAdapter.pokemonCaptured(148, R.drawable.dragonite);
				}
				if (result.equals("mewtwo")
						|| sharedPreferences.getString("mewtwo", "").equals(
								"mewtwo")) {
					imageAdapter.pokemonCaptured(149, R.drawable.mewtwo);
				}
				if (result.equals("mew")
						|| sharedPreferences.getString("mew", "").equals("mew")) {
					imageAdapter.pokemonCaptured(150, R.drawable.mew);
				}

			}
			// mTextView.setText("Read content: " + result);
			// ImageView pikachu = (ImageView) findViewById(R.id.pikachu);
		}
	}

}
