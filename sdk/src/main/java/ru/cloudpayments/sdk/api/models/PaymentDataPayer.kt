package ru.cloudpayments.sdk.api.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PaymentDataPayer(
	@SerializedName("FirstName") var firstName: String? = null,
	@SerializedName("LastName") var lastName: String? = null,
	@SerializedName("MiddleName") var middleName: String? = null,
	@SerializedName("Birth") var birthDay: String? = null,
	@SerializedName("Address") var address: String? = null,
	@SerializedName("Street") var street: String? = null,
	@SerializedName("City") var city: String? = null,
	@SerializedName("Country") var country: String? = null,
	@SerializedName("Phone") var phone: String? = null,
	@SerializedName("Postcode") var postcode: String? = null) : Parcelable
