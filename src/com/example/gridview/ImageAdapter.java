package com.example.gridview;

import android.R.drawable;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	private Context mContext;

	public ImageAdapter(Context c) {
		mContext = c;
	}

	public int getCount() {
		return mThumbIds.length;
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) { // if it's not recycled, initialize some
									// attributes
			imageView = new ImageView(mContext);
			imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(8, 8, 8, 8);
		} else {
			imageView = (ImageView) convertView;
		}

		imageView.setImageResource(mThumbIds[position]);
		return imageView;
	}

	public void pokemonCaptured(int pos, int id) {
		// int id = getResources().getIdentifier(s, "drawable",
		// "com.example.gridview");
		// mThumbIds[0]= R.drawable.class.getField(s).getInt(null);
		mThumbIds[pos] = id;

		notifyDataSetChanged(); // add this method
	}

	// references to our images
	private Integer[] mThumbIds = { R.drawable.graybulbasaur,
			R.drawable.grayivysaur, R.drawable.grayvenusaur,
			R.drawable.graysquirtle, R.drawable.graywartortle,
			R.drawable.grayblastoise, R.drawable.graycharmander,
			R.drawable.graycharmeleon, R.drawable.graycharizard,
			R.drawable.graycaterpie, R.drawable.graymetapod,
			R.drawable.graybutterfree, R.drawable.grayweedle,
			R.drawable.graykakuna, R.drawable.graybeedrill,
			R.drawable.graypidgey, R.drawable.graypidgeotto,
			R.drawable.graypidgeot, R.drawable.grayrattata,
			R.drawable.grayraticate, R.drawable.grayspearow,
			R.drawable.grayfearow, R.drawable.grayekans, R.drawable.grayarbok,
			R.drawable.graypikachu, R.drawable.grayraichu,
			R.drawable.graysandshrew, R.drawable.graysandslash,
			R.drawable.graynidoranf, R.drawable.graynidorina,
			R.drawable.graynidoqueen, R.drawable.graynidoranm,
			R.drawable.graynidorino, R.drawable.graynidoking,
			R.drawable.grayclefairy, R.drawable.grayclefable,
			R.drawable.grayvulpix, R.drawable.grayninetales,
			R.drawable.grayjigglypuff, R.drawable.graywigglytuff,
			R.drawable.grayzubat, R.drawable.graygolbat, R.drawable.grayoddish,
			R.drawable.graygloom, R.drawable.grayvileplume,
			R.drawable.grayparas, R.drawable.grayparasect,
			R.drawable.grayvenonat, R.drawable.grayvenomoth,
			R.drawable.graydiglett, R.drawable.graydugtrio,
			R.drawable.graymeowth, R.drawable.graypersian,
			R.drawable.graypsyduck, R.drawable.graygolduck,
			R.drawable.graymankey, R.drawable.grayprimeape,
			R.drawable.graygrowlithe, R.drawable.grayarcanine,
			R.drawable.graypoliwag, R.drawable.graypoliwhirl,
			R.drawable.graypoliwrath, R.drawable.grayabra,
			R.drawable.graykadabra, R.drawable.grayalakazam,
			R.drawable.graymachop, R.drawable.graymachoke,
			R.drawable.graymachamp, R.drawable.graybellsprout,
			R.drawable.grayweepinbell, R.drawable.grayvictreebel,
			R.drawable.graytentacool, R.drawable.graytentacruel,
			R.drawable.graygeodude, R.drawable.graygraveler,
			R.drawable.graygolem, R.drawable.grayponyta,
			R.drawable.grayrapidash, R.drawable.grayslowpoke,
			R.drawable.grayslowbro, R.drawable.graymagnemite,
			R.drawable.graymagneton, R.drawable.grayfarfetchd,
			R.drawable.graydoduo, R.drawable.graydodrio, R.drawable.grayseel,
			R.drawable.graydewgong, R.drawable.graygrimer, R.drawable.graymuk,
			R.drawable.grayshellder, R.drawable.graycloyster,
			R.drawable.graygastly, R.drawable.grayhaunter,
			R.drawable.graygengar, R.drawable.grayonix, R.drawable.graydrowzee,
			R.drawable.grayhypno, R.drawable.graykrabby,
			R.drawable.graykingler, R.drawable.grayvoltorb,
			R.drawable.grayelectrode, R.drawable.grayexeggcute,
			R.drawable.grayexeggutor, R.drawable.graycubone,
			R.drawable.graymarowak, R.drawable.grayhitmonlee,
			R.drawable.grayhitmonchan, R.drawable.graylickitung,
			R.drawable.graykoffing, R.drawable.grayweezing,
			R.drawable.grayrhyhorn, R.drawable.grayrhydon,
			R.drawable.graychansey, R.drawable.graytangela,
			R.drawable.graykangaskhan, R.drawable.grayhorsea,
			R.drawable.grayseadra, R.drawable.graygoldeen,
			R.drawable.grayseaking, R.drawable.graystaryu,
			R.drawable.graystarmie, R.drawable.graymrmime,
			R.drawable.grayscyther, R.drawable.grayjynx,
			R.drawable.grayelectabuzz, R.drawable.graymagmar,
			R.drawable.graypinsir, R.drawable.graytauros,
			R.drawable.graymagikarp, R.drawable.graygyarados,
			R.drawable.graylapras, R.drawable.grayditto, R.drawable.grayeevee,
			R.drawable.grayvaporeon, R.drawable.grayjolteon,
			R.drawable.grayflareon, R.drawable.grayporygon,
			R.drawable.grayomanyte, R.drawable.grayomastar,
			R.drawable.graykabuto, R.drawable.graykabutops,
			R.drawable.grayaerodactyl, R.drawable.graysnorlax,
			R.drawable.grayarticuno, R.drawable.grayzapdos,
			R.drawable.graymoltres, R.drawable.graydratini,
			R.drawable.graydragonair, R.drawable.graydragonite,
			R.drawable.graymewtwo, R.drawable.graymew, };
}
