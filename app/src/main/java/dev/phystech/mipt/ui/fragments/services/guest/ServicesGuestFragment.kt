package dev.phystech.mipt.ui.fragments.services.guest

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.repositories.UserRepository
import dev.phystech.mipt.ui.activities.authorization.AuthorizationActivity

class ServicesGuestFragment: BaseFragment() {

    lateinit var rlLogin: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_services_guest, container, false)
    }


    override fun bindView(view: View?) {
        if (view == null) return

        rlLogin = view.findViewById(R.id.rlLogin)
        rlLogin.setOnClickListener {
            UserRepository.shared.clear()
            val authIntent = Intent(requireContext(), AuthorizationActivity::class.java)
            startActivity(authIntent)
            activity?.finish()
        }
    }
}