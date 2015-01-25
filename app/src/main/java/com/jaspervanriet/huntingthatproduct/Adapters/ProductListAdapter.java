/*
 * Copyright (C) 2015 Jasper van Riet
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaspervanriet.huntingthatproduct.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jaspervanriet.huntingthatproduct.Activities.Settings.SettingsActivity;
import com.jaspervanriet.huntingthatproduct.Classes.Product;
import com.jaspervanriet.huntingthatproduct.R;
import com.jaspervanriet.huntingthatproduct.Utils.Utils;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>
		implements View.OnClickListener {

	private final static int ANIM_LIST_ENTER_DURATION = 700;
	private ArrayList<Product> mProducts;
	private Context mContext;
	private OnProductClickListener mOnProductClickListener;
	private int lastAnimatedPosition = -1;
	private static final int ANIMATED_ITEMS_COUNT = 3;

	public ProductListAdapter (Context context, ArrayList<Product> mProducts) {
		this.mContext = context;
		this.mProducts = mProducts;
	}

	@Override
	public int getItemCount () {
		return mProducts.size ();
	}

	@Override
	public void onBindViewHolder (final ProductViewHolder holder, int position) {
		runEnterAnimation (holder.itemView, position);
		holder.screenshotRipple.setTag (position);
		holder.detailsLayout.setTag (position);
		loadCardText (holder);
		loadImage (holder);
	}

	private void loadCardText (ProductViewHolder holder) {
		holder.title.setText (mProducts.get (holder.getPosition ()).title);
		holder.title.setTypeface (
				Typeface.createFromAsset (mContext.getAssets (), "fonts/Roboto-Light.ttf"));
		holder.description.setText (mProducts.get (holder.getPosition ()).tagline);
		holder.votes.setText (mProducts.get (holder.getPosition ()).votes
				+ " " + mContext.getResources ().getString (R.string.votes));
		holder.comments.setText (mProducts.get (holder.getPosition ()).numberOfComments
				+ " " + mContext.getResources ().getString (R.string.comments));
	}

	private void loadImage (final ProductViewHolder holder) {
		String imgUrl;
		if (!highQualityImages () && !isUserOnWifi ()) {
			imgUrl = mProducts.get (holder.getPosition ()).smallImgUrl;
		} else {
			imgUrl = mProducts.get (holder.getPosition ()).largeImgUrl;
		}

		holder.progressWheel.setVisibility (View.VISIBLE);
		holder.progressWheel.spin ();
		Picasso.with (mContext).load (imgUrl)
				.resize (380, 250)
				.centerCrop ()
				.into (holder.screenshot, new Callback () {
					@Override
					public void onSuccess () {
						holder.progressWheel.setVisibility (View.GONE);
						holder.progressWheel.stopSpinning ();
					}

					@Override
					public void onError () {
					}
				});
	}

	private boolean highQualityImages () {
		SettingsActivity settingsActivity = new SettingsActivity ();
		return settingsActivity.getHighQualityImagesPref
				(mContext);
	}

	@Override
	public ProductViewHolder onCreateViewHolder (ViewGroup viewGroup, final int i) {
		View itemView = LayoutInflater.
				from (viewGroup.getContext ()).
				inflate (R.layout.item_product_card, viewGroup, false);

		ProductViewHolder holder = new ProductViewHolder (itemView);
		holder.screenshotRipple.setOnClickListener (this);
		holder.detailsLayout.setOnClickListener (this);
		return holder;
	}

	@Override
	public void onClick (View v) {
		if (v.getId () == R.id.card_product_image_ripple) {
			mOnProductClickListener.onImageClick (v, (Integer) v.getTag ());
		} else if (v.getId () == R.id.card_product_details_frame) {
			mOnProductClickListener.onDetailsClick (v, mProducts.get ((Integer) v.getTag ()));
		}
	}

	private void runEnterAnimation (View view, int position) {
		if (position >= ANIMATED_ITEMS_COUNT - 1) {
			return;
		}
		if (position > lastAnimatedPosition) {
			lastAnimatedPosition = position;
			view.setTranslationY (Utils.getScreenHeight (mContext));
			view.animate ()
					.translationY (0)
					.setInterpolator (new DecelerateInterpolator (3.f))
					.setDuration (ANIM_LIST_ENTER_DURATION)
					.start ();
		}
	}

	private boolean isUserOnWifi () {
		ConnectivityManager connManager = (ConnectivityManager)
				mContext.getSystemService (Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connManager.getNetworkInfo (ConnectivityManager
				.TYPE_WIFI);
		return wifi.isConnected ();
	}

	public void setOnProductClickListener (OnProductClickListener onProductClickListener) {
		this.mOnProductClickListener = onProductClickListener;
	}

	public static class ProductViewHolder extends RecyclerView.ViewHolder {

		@InjectView (R.id.card_product_title)
		TextView title;
		@InjectView (R.id.card_product_description)
		TextView description;
		@InjectView (R.id.card_product_image)
		ImageView screenshot;
		@InjectView (R.id.card_product_image_ripple)
		View screenshotRipple;
		@InjectView (R.id.card_product_upvotes)
		TextView votes;
		@InjectView (R.id.card_product_comments)
		TextView comments;
		@InjectView (R.id.card_product_progress_wheel)
		ProgressWheel progressWheel;
		@InjectView (R.id.card_product_details_frame)
		RelativeLayout detailsLayout;


		public ProductViewHolder (View view) {
			super (view);
			ButterKnife.inject (this, view);
		}
	}

	public interface OnProductClickListener {
		public void onImageClick (View v, int position);

		public void onDetailsClick (View v, Product product);
	}
}
