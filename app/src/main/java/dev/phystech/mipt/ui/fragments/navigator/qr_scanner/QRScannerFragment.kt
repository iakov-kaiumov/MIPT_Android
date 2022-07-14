package dev.phystech.mipt.ui.fragments.navigator.qr_scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.Preview
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.zxing.Result
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
//import com.google.common.util.concurrent.ListenableFuture
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.LegendModel
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.repositories.HistoryRepository
import dev.phystech.mipt.repositories.PlacesRepository
import dev.phystech.mipt.ui.fragments.navigator.legend_detail.LegendDetailFragment
import dev.phystech.mipt.ui.fragments.navigator.place_detail.PlaceDetailFragment

class QRScannerFragment: BaseFragment(), DecodeCallback {

    private var codeScanner: CodeScanner? = null
    private lateinit var ivBack: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_qr_code_scanner, container, false)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PERMISSION_REQUEST_CODE_CAMERA && checkCameraPermission()) {
            startScanning()
        }
    }

    override fun onResume() {
        super.onResume()
        permissionPrepare()
    }

    override fun onPause() {
        codeScanner?.releaseResources()
        super.onPause()
    }


    override fun bindView(view: View?) {
        if (view == null) return

        ivBack = view.findViewById(R.id.ivBack)
        permissionPrepare()

        ivBack.setOnClickListener {
            navigationPresenter.popFragment()
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun permissionPrepare() {
        if (checkCameraPermission()) {
            startScanning()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CODE_CAMERA)
    }

    private fun startScanning() {
        val view = view ?: return

        if (codeScanner == null) {
            codeScanner = CodeScanner(requireContext(), view.findViewById(R.id.codeScannerView))

            codeScanner?.apply {
                camera = CodeScanner.CAMERA_BACK
                formats = CodeScanner.ALL_FORMATS

                autoFocusMode = AutoFocusMode.SAFE
                scanMode = ScanMode.CONTINUOUS
                isAutoFocusEnabled = true
                isFlashEnabled = false

                decodeCallback = this@QRScannerFragment
            }
        }

        codeScanner?.startPreview()
    }


    companion object {
        const val PERMISSION_REQUEST_CODE_CAMERA = 55001
    }


    //  QR Decoder callback
    override fun onDecoded(result: Result) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), result.text, Toast.LENGTH_LONG).show()
            codeScanner?.releaseResources()

            val entityCode = LinkDecoder.shared.getCodeFromLink(result.text)

            if (entityCode == null) {
                showInBrowser(result.text)
                Toast.makeText(requireContext(), "некорректный код", Toast.LENGTH_SHORT).show()
            } else {
                ApiClient.shared.qrCodeValues(entityCode)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        val model = it.data?.paginator?.values?.firstOrNull()
                        if (model == null) {
                            showInBrowser(result.text)
                            Toast.makeText(requireContext(), "некорректный код", Toast.LENGTH_SHORT).show()
                            return@subscribe
                        }

                        if (model.places.isNotEmpty()) {
                            val placeToShow = model.places.first()
                            PlacesRepository.shared.getById(placeToShow.id ?: "") {
                                if (it == null) {
                                    showInBrowser(result.text)
                                    Toast.makeText(requireContext(), "некорректный код", Toast.LENGTH_SHORT).show()
                                } else {
                                    val fragment = PlaceDetailFragment(it)
                                    navigationPresenter.popFragment()
                                    navigationPresenter.pushFragment(fragment, true)
                                }
                            }
                        } else if (model.posts.isNotEmpty()) {
                            val postToShow = model.posts.first()
                            HistoryRepository.shared.getById(postToShow.id ?: "") {
                                if (it == null) {
                                    showInBrowser(result.text)
                                    Toast.makeText(requireContext(), "некорректный код", Toast.LENGTH_SHORT).show()
                                } else {
                                    val fragment = LegendDetailFragment(it)
                                    navigationPresenter.popFragment()
                                    navigationPresenter.pushFragment(fragment, true)
                                }
                            }
                        }

                    }, {
                        showInBrowser(result.text)
                        Toast.makeText(requireContext(), "некорректный код", Toast.LENGTH_SHORT).show()
                    })
            }



        }
    }


    //  OTHERS
    private fun showInBrowser(link: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        startActivity(intent)
        navigationPresenter.popFragment()
    }


    class LinkDecoder private constructor() {

        fun getCodeFromLink(link: String): String? {
            val regex = Regex("^https://app\\.mipt\\.ru/qr/([0-9a-z]+)$")
            if (regex.matches(link)) {
                val regResult =  regex.matchEntire(link)
                regResult?.groups?.first { v -> v != null && v.value != link}?.let { match ->
                    return match.value
                }
            }

            return null
        }

        companion object {
            val shared: LinkDecoder = LinkDecoder()
        }
    }

}