package com.karry.ohmychat.ui.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.karry.ohmychat.R
import com.karry.ohmychat.databinding.FragmentProfileBinding
import com.karry.ohmychat.utils.checkAndRequestPermissions
import com.karry.ohmychat.utils.getColorResource
import com.karry.ohmychat.utils.setBackgroundColor
import com.karry.ohmychat.utils.setIconColor
import java.io.FileNotFoundException

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var bitmap: Bitmap? = null
    private var uri: Uri? = null
    private lateinit var profileImageToolbar: ImageView
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        if (bitmap != null) {
            binding.myProfileImage.setImageBitmap(bitmap)
            profileImageToolbar.setImageBitmap(bitmap)
        }

        init()
        return binding.root
    }

    private fun init() {
        profileImageToolbar = requireActivity().findViewById(R.id.profile_image_home)
        with(binding) {

            setIconColor(
                requireActivity(),
                R.drawable.ic_camera,
                myButtonCamera,
                R.color.profiles_900
            )
            setIconColor(requireActivity(), R.drawable.ic_edit, buttonEdit, R.color.profiles_900)
            setIconColor(
                requireActivity(),
                R.drawable.ic_setting,
                buttonSettingProfile,
                R.color.profiles_900
            )
            setBackgroundColor(
                myBioProfile.background!!,
                getColorResource(requireActivity(), R.color.profiles_200_tran_BA)
            )
            setBackgroundColor(
                myButtonCamera.background!!,
                getColorResource(requireActivity(), R.color.profiles_200_tran_5A)
            )
            setBackgroundColor(
                myEmailProfile.background!!,
                getColorResource(requireActivity(), R.color.profiles_200_tran_BA)
            )
            myProfileImage.setOnClickListener {
                val action =
                    ViewPaperFragmentDirections.actionViewPaperFragmentToPhotoViewFragment(bitmap)
                findNavController().navigate(action)
            }
            myButtonCamera.setOnClickListener {
                val buttonClick = AlphaAnimation(1f, 0.8f)
                it.startAnimation(buttonClick)
                showPopupMenu(it, R.menu.photo_menu)
            }

        }
    }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null) {
            uri = it.data!!.data!!
            try {
                val inputStream = requireActivity().contentResolver.openInputStream(uri!!)
                bitmap = BitmapFactory.decodeStream(inputStream)
                binding.myProfileImage.setImageBitmap(bitmap)
                profileImageToolbar.setImageBitmap(bitmap)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null) {
            bitmap = it!!.data!!.extras!!.get("data") as Bitmap
            binding.myProfileImage.setImageBitmap(bitmap)
            profileImageToolbar.setImageBitmap(bitmap)
        }
    }

    private fun showPopupMenu(v: View, @MenuRes menuRes: Int) {
        val popupMenu = PopupMenu(requireContext(), v)
        popupMenu.menuInflater.inflate(menuRes, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener when(it.itemId) {
                R.id.choose_gallery -> {
                    val intent = Intent(
                        Intent.ACTION_PICK, MediaStore.Images.Media
                            .EXTERNAL_CONTENT_URI).apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    pickImage.launch(intent)
                    true
                }
                R.id.take_photo -> {
                    if (checkAndRequestPermissions(requireActivity())) {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        takePhoto.launch(intent)
                    }
                    true
                }
                else -> false
            }
        }

        try {
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenu)
            menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(menu, true)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            popupMenu.show()
        }
    }
}