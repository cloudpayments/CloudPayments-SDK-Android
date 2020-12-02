package ru.cloudpayments.demo.screens.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_list_cart.view.*
import ru.cloudpayments.demo.models.Product
import ru.cloudpayments.demo.R

class CartHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
	fun bind(item: Product) {
		with(itemView) {
			text_name.text = item.name
			text_price.text = item.price + " " + context.getString(R.string.main_rub)

			Glide
				.with(context)
				.load(item.imageUrl)
				.apply(RequestOptions.bitmapTransform(CenterCrop()))
				.into(image_product)
		}
	}

	companion object {
		fun create(parent: ViewGroup): CartHolder {
			return CartHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list_cart, parent, false))
		}
	}
}