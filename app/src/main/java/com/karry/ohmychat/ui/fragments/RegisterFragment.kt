package com.karry.ohmychat.ui.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.util.PatternsCompat.EMAIL_ADDRESS
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.karry.ohmychat.R
import com.karry.ohmychat.databinding.FragmentRegisterBinding
import com.karry.ohmychat.ui.activities.MainActivity
import com.karry.ohmychat.utils.*
import com.karry.ohmychat.viewmodel.RegisterViewModel
import java.io.FileNotFoundException
import java.util.*


class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private var bitmap: Bitmap? = null
    private var uri: Uri? = null
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var currentUser: FirebaseUser

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        bitmap = null
        uri = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        registerViewModel = ViewModelProvider(
            requireActivity(), ViewModelProvider
                .AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(RegisterViewModel::class.java)

        if (bitmap != null) {
            binding.profileImageRegister.setImageBitmap(bitmap)
        }

        with(binding) {
            val buttonClick = AlphaAnimation(1f, 0.8f)

            setIconColor(R.drawable.ic_camera, buttonUpImage, R.color.camera_icon_register)
            setBackgroundColor(buttonUpImage.background!!, getColorResource(R.color.camera_icon_register_background))


            buttonToLogin.setOnClickListener {
                activity?.onBackPressed()
            }

            profileImageRegister.setOnClickListener {
                val action = RegisterFragmentDirections.actionRegisterFragmentToPhotoViewFragment(bitmap)
                findNavController().navigate(action)
            }

            buttonRegister.setOnClickListener {
                usernameRegisterInputLayout.clearFocus()
                emailRegisterInputLayout.clearFocus()
                passwordRegisterInputLayout.clearFocus()
                it.startAnimation(buttonClick)

                val email = emailRegisterEditText.text.toString()
                val name = usernameRegisterEditText.text.toString()
                val password = passwordRegisterEditText.text.toString()
                val passwordConfirm = confirmRegisterEditText.text.toString()

                if (email.isEmpty() && name.isEmpty() && password.isEmpty() && passwordConfirm.isEmpty()) {
                    showToast(requireContext(), "Fields are empty!")
                    usernameRegisterInputLayout.requestFocus()
                } else if (name.isEmpty()) {
                    usernameRegisterInputLayout.error = "Please enter a username."
                    usernameRegisterInputLayout.requestFocus()
                } else if (email.isEmpty()) {
                    emailRegisterInputLayout.error = "Please enter your email."
                    emailRegisterInputLayout.requestFocus()
                } else if(!EMAIL_ADDRESS.matcher(email).matches()) {
                    emailRegisterInputLayout.error = "Your text is not email."
                    emailRegisterInputLayout.requestFocus()
                } else if (password.isEmpty()) {
                    passwordRegisterInputLayout.error = "Please enter your password"
                    passwordRegisterInputLayout.requestFocus()
                } else if (passwordConfirm.isEmpty()) {
                    confirmRegisterInputLayout.error = "Please enter your password"
                    confirmRegisterInputLayout.requestFocus()
                } else if(password != passwordConfirm) {
                    confirmRegisterInputLayout.error = "Password confirm and password are not " +
                            "similar"
                    confirmRegisterInputLayout.requestFocus()
                } else {
                    progressBarRegister.visibility = View.VISIBLE
                    usernameRegisterInputLayout.isClickable = false
                    emailRegisterInputLayout.isClickable = false
                    passwordRegisterInputLayout.isClickable = false
                    buttonRegister.isClickable = false
                    buttonToLogin.isClickable = false
                    profileImageRegister.isClickable = false
                    buttonUpImage.isClickable = false
                    buttonRegister.animate().alpha(0.5F).duration = 500L

                    usernameRegisterEditText.setText("")
                    emailRegisterEditText.setText("")
                    passwordRegisterEditText.setText("")
                    confirmRegisterEditText.setText("")

                    dismissKeyboard(requireActivity())
                    register(email, password)
                }
            }



            buttonUpImage.setOnClickListener {
                it.startAnimation(buttonClick)
                showPopupMenu(it, R.menu.photo_menu)
            }
        }

        return binding.root
    }


    private fun register(email: String, password: String) {
        registerViewModel.registerUser(email, password)
        registerViewModel.registerUser.observe(viewLifecycleOwner) {
            if(!it.isSuccessful) {
                with(binding) {
                    progressBarRegister.visibility = View.GONE
                    usernameRegisterInputLayout.isClickable = true
                    emailRegisterInputLayout.isClickable = true
                    passwordRegisterInputLayout.isClickable = true
                    buttonRegister.isClickable = true
                    buttonToLogin.isClickable = true
                    buttonRegister.animate().alpha(1F).duration = 500L
                    profileImageRegister.isClickable = true
                    buttonUpImage.isClickable = true

                    usernameRegisterInputLayout.requestFocus()

                }
                try {
                    throw it.exception!!
                } catch (existEmail: FirebaseAuthUserCollisionException) {
                    Toast.makeText(context, "Email Id already exists.", Toast.LENGTH_SHORT).show()
                } catch (weakPassword: FirebaseAuthWeakPasswordException) {
                    Toast.makeText(
                        context,
                        "Password length should be more then six characters.",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (malformedEmail: FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(
                        context,
                        "Invalid credentials, please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: java.lang.Exception) {
                    Toast.makeText(context, "SignUp unsuccessful. Try again.", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                getUserSession()
                val intent = Intent(requireActivity(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()


            }
        }
    }

    private fun getUserSession() {
        registerViewModel.userFirebaseSession.observe(viewLifecycleOwner) {
            currentUser = it
        }
    }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null) {
            uri = it.data!!.data!!
            try {
                val inputStream = requireActivity().contentResolver.openInputStream(uri!!)
                bitmap = BitmapFactory.decodeStream(inputStream)
                binding.profileImageRegister.setImageBitmap(bitmap)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null) {
            bitmap = it!!.data!!.extras!!.get("data") as Bitmap
            binding.profileImageRegister.setImageBitmap(bitmap)
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

    private fun getColorResource(@ColorRes color: Int) =
        ContextCompat.getColor(requireActivity().applicationContext, color)

    private fun setIconColor(@DrawableRes iconRes: Int, button: ImageView, @ColorRes colorRes: Int) {
        var drawable = ContextCompat.getDrawable(requireActivity().applicationContext, iconRes)
        drawable = DrawableCompat.wrap(drawable!!)
        DrawableCompat.setTint(drawable, getColorResource(colorRes))
        button.setImageDrawable(drawable)
    }

    private fun setBackgroundColor(background: Drawable, @ColorInt color: Int) {
        if (background is ShapeDrawable) {
            background.paint.color = color
        } else if (background is GradientDrawable) {
            background.setColor(color)
        } else if (background is ColorDrawable) {
            background.color = color
        }
    }

}