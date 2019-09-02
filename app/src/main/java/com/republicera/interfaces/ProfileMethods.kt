package com.republicera.interfaces

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import android.widget.TextView
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.groupieAdapters.SingleContactChannelOption
import com.republicera.groupieAdapters.SingleLanguageOption
import com.republicera.models.ContactChannelOption
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

interface ProfileMethods : GeneralMethods {

    fun notFollowedButton(followButton: TextView, activity: Activity) {
        followButton.setBackgroundResource(R.drawable.button_curve_16_500)
        followButton.setTextColor(ContextCompat.getColor(activity, R.color.white))
        followButton.text = activity.getString(R.string.follow)
    }

    fun followedButton(followButton: TextView, activity: Activity) {
        followButton.setBackgroundResource(R.drawable.unfollow_button)
        followButton.setTextColor(ContextCompat.getColor(activity, R.color.gray300))
        followButton.text = activity.getString(R.string.unfollow)
    }

    fun loadContactChannelOptions(
        adapter: GroupAdapter<ViewHolder>,
        userInput: String,
        activity: MainActivity
    ): Boolean {

        val optionsList = listOf(
            SingleContactChannelOption(ContactChannelOption(0, "Email")),
            SingleContactChannelOption(
                ContactChannelOption(1, "Phone")
            ),
            SingleContactChannelOption(
                ContactChannelOption(2, "WhatsApp")
            ),
            SingleContactChannelOption(
                ContactChannelOption(3, "Twitter")
            ),
            SingleContactChannelOption(
                ContactChannelOption(4, "Instagram")
            ),
            SingleContactChannelOption(
                ContactChannelOption(5, "Website")
            ),
            SingleContactChannelOption(
                ContactChannelOption(6, "Linkedin")
            ),
            SingleContactChannelOption(
                ContactChannelOption(7, "Facebook")
            ),
            SingleContactChannelOption(
                ContactChannelOption(8, "Medium")
            ),
            SingleContactChannelOption(
                ContactChannelOption(9, "YouTube")
            ),
            SingleContactChannelOption(
                ContactChannelOption(10, "Snapchat")
            )
        )

        adapter.clear()
        adapter.addAll(optionsList.filter { it.contactChannel.title.toLowerCase().startsWith(userInput.toLowerCase()) })

        return true
    }



    fun onClickContactIcon(case: Int, intentData: String, activity: Activity) {

        /*
cases:
email = 0
phone = 1
whatsApp = 2
twitter = 3
instagram = 4
website = 5
linkedin = 6
facebook = 7
medium = 8
youtube = 9
snapchat = 10
*/

        when (case) {
            0 -> {
                val emailIntent = Intent(Intent.ACTION_SENDTO)
                emailIntent.data = Uri.parse("mailto:$intentData")
                activity.startActivity(emailIntent)
            }
            1 -> {
                activity.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$intentData")))
            }
            2 -> {
                val url = "https://api.whatsapp.com/send?phone=$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }

            3 -> {
                val url = "https://www.twitter.com/$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }

            4 -> {
                val url = "https://www.instagram.com/$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            5 -> {
                val url = "https://$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            6 -> {
                val url = "https://www.linkedin.com/in/$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            7 -> {
                val url = "https://www.facebook.com/$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            8 -> {
                val url = "https://medium.com/$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            9 -> {
                val url = "https://www.youtube.com/user/$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            10 -> {
                val url = "https://www.snapchat.com/$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
    }


    fun loadLanguageOptions(adapter: GroupAdapter<ViewHolder>, currentList: List<String>) {
        adapter.clear()
        val languagesList = listOf(
            Pair("af", "Afrikaans"),
            Pair("am", "Amharic"),
            Pair("ar", "Arabic"),
//            Pair("ar-Latn", "Arabic"),
            Pair("az", "Azerbaijani"),
            Pair("be", "Belarusian"),
            Pair("bg", "Bulgarian"),
//            Pair("bg-Latn", "Bulgarian"),
            Pair("bn", "Bengali"),
            Pair("bs", "Bosnian"),
            Pair("ca", "Catalan"),
            Pair("ceb", "Cebuano"),
            Pair("co", "Corsican"),
            Pair("cs", "Czech"),
            Pair("cy", "Welsh"),
            Pair("da", "Danish"),
            Pair("de", "German"),
            Pair("el", "Greek"),
//            Pair("el-Latn", "Greek"),
            Pair("en", "English"),
            Pair("eo", "Esperanto"),
            Pair("es", "Spanish"),
            Pair("et", "Estonian"),
            Pair("eu", "Basque"),
            Pair("fa", "Persian"),
            Pair("fi", "Finnish"),
            Pair("fil", "Filipino"),
            Pair("fr", "French"),
            Pair("fy", "Western Frisian"),
            Pair("ga", "Irish"),
            Pair("gd", "Scots Gaelic"),
            Pair("gl", "Galician"),
            Pair("gu", "Gujarati"),
            Pair("ha", "Hausa"),
            Pair("haw", "Hawaiian"),
            Pair("hi", "Hindi"),
//            Pair("hi-Latn", "Hindi"),
            Pair("hmn", "Hmong"),
            Pair("hr", "Croatian"),
            Pair("ht", "Haitian"),
            Pair("hu", "Hungarian"),
            Pair("hy", "Armenian"),
            Pair("id", "Indonesian"),
            Pair("ig", "Igbo"),
            Pair("is", "Icelandic"),
            Pair("it", "Italian"),
            Pair("iw", "Hebrew"),
            Pair("ja", "Japanese"),
//            Pair("ja-Latn", "Japanese"),
            Pair("jv", "Javanese"),
            Pair("ka", "Georgian"),
            Pair("kk", "Kazakh"),
            Pair("km", "Khmer"),
            Pair("kn", "Kannada"),
            Pair("ko", "Korean"),
            Pair("ku", "Kurdish"),
            Pair("ky", "Kyrgyz"),
            Pair("la", "Latin"),
            Pair("lb", "Luxembourgish"),
            Pair("lo", "Lao"),
            Pair("lt", "Lithuanian"),
            Pair("lv", "Latvian"),
            Pair("mg", "Malagasy"),
            Pair("mi", "Maori"),
            Pair("mk", "Macedonian"),
            Pair("ml", "Malayalam"),
            Pair("mn", "Mongolian"),
            Pair("mr", "Marathi"),
            Pair("ms", "Malay"),
            Pair("mt", "Maltese"),
            Pair("my", "Burmese"),
            Pair("ne", "Nepali"),
            Pair("nl", "Dutch"),
            Pair("no", "Norwegian"),
            Pair("ny", "Nyanja"),
            Pair("pa", "Punjabi"),
            Pair("pl", "Polish"),
            Pair("ps", "Pashto"),
            Pair("pt", "Portuguese"),
            Pair("ro", "Romanian"),
            Pair("ru", "Russian"),
//            Pair("ru-Latn", "Russian"),
            Pair("sd", "Sindhi"),
            Pair("si", "Sinhala"),
            Pair("sk", "Slovak"),
            Pair("sl", "Slovenian"),
            Pair("sm", "Samoan"),
            Pair("sn", "Shona"),
            Pair("so", "Somali"),
            Pair("sq", "Albanian"),
            Pair("sr", "Serbian"),
            Pair("st", "Sesotho"),
            Pair("su", "Sundanese"),
            Pair("sv", "Swedish"),
            Pair("sw", "Swahili"),
            Pair("ta", "Tamil"),
            Pair("te", "Telugu"),
            Pair("tg", "Tajik"),
            Pair("th", "Thai"),
            Pair("tr", "Turkish"),
            Pair("uk", "Ukrainian"),
            Pair("ur", "Urdu"),
            Pair("uz", "Uzbek"),
            Pair("vi", "Vietnamese"),
            Pair("xh", "Xhosa"),
            Pair("yi", "Yiddish"),
            Pair("yo", "Yoruba"),
            Pair("zh", "Chinese"),
//            Pair("zh-Latn", "Chinese"),
            Pair("zu", "Zulu")
        )

        val adapterLanguagesList = mutableListOf<SingleLanguageOption>()

        for (language in languagesList) {
            adapterLanguagesList.add(SingleLanguageOption(language, currentList))
        }

        adapter.addAll(adapterLanguagesList)
    }


}

/*

expected types:
0 = email
1 = web address
2 = Int

cases:
email = 0
phone = 1
whatsApp = 2
twitter = 3
instagram = 4
website = 5
linkedin = 6
facebook = 7
medium = 8
youtube = 9
snapchat = 10

*/
