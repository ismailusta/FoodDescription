package com.example.fooddescription

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.fooddescription.databinding.FragmentFoodListBinding
import java.io.ByteArrayOutputStream
import kotlin.reflect.typeOf


class FoodAdding : Fragment() {


    private var secilenBitmap: Bitmap? = null
    private lateinit var foodnameEditText: EditText
    private lateinit var ingredientsEditText: EditText
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_food_adding, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        foodnameEditText = view.findViewById(R.id.textViewFoodName)
        ingredientsEditText = view.findViewById(R.id.textViewFoodIngredients)
        view.findViewById<Button>(R.id.btnSave).setOnClickListener {
            kaydet(it)

        }
        view.findViewById<ImageView>(R.id.imageView2).setOnClickListener {
            gorselsec(it)
        }
        arguments?.let {
            val gelenBilgi = FoodAddingArgs.fromBundle(it).bilgi
            if (gelenBilgi==false) {
                view.findViewById<EditText>(R.id.textViewFoodName).setText("")
                view.findViewById<EditText>(R.id.textViewFoodIngredients).setText("")
                view.findViewById<Button>(R.id.btnSave).visibility = View.VISIBLE
                val gorsel = BitmapFactory.decodeResource(context?.resources, R.drawable.a)
                view.findViewById<ImageView>(R.id.imageView2).setImageBitmap(gorsel)
            }
            else {
                view.findViewById<Button>(R.id.btnSave).visibility = View.INVISIBLE
                val gelenId = FoodAddingArgs.fromBundle(it).id
                context?.let {
                    try {
                        val db = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE, null)
                        val cursor = db.rawQuery("SELECT * FROM yemekler WHERE id = ?", arrayOf(gelenId.toString()))
                        val foodNameIndex = cursor.getColumnIndex("foodname")
                        val descripeIndex = cursor.getColumnIndex("descripe")
                        val imageIndex = cursor.getColumnIndex("image")
                        while (cursor.moveToNext()){
                            foodnameEditText.setText(cursor.getString(foodNameIndex))
                            ingredientsEditText.setText(cursor.getString(descripeIndex))
                            val byteDizisi = cursor.getBlob(imageIndex)
                            val bitmap = BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)
                            view.findViewById<ImageView>(R.id.imageView2).setImageBitmap(bitmap)
                        }
                        cursor.close()
                    }
                    catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
            }
        }

    }

    private fun kaydet(view: View) {
        val foodname = foodnameEditText.text.toString()
        val descripe = ingredientsEditText.text.toString()
        if (secilenBitmap != null) {
            val kucukBoyutlu = gorselboyutunuduzenle(secilenBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            kucukBoyutlu.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteDizisi = outputStream.toByteArray()
            try {
                context?.let {
                    val database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE, null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY,foodname VARCHAR,descripe VARCHAR,image BLOB)")
                    val sqlString = "INSERT INTO yemekler (foodname,descripe,image) VALUES (?,?,?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1, foodname).toString()
                    statement.bindString(2, descripe)
                    statement.bindBlob(3, byteDizisi)
                    statement.execute() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            println("NAVİGASYONA KADAR İNDİ")
            val action = FoodAddingDirections.actionFoodAddingToFoodList2()
            Navigation.findNavController(view).navigate(action)

        }
    }

    private fun gorselsec(view: View) {
        activity?.let {
            if (ContextCompat.checkSelfPermission(
                    it.applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            } else {
                // İzin zaten verilmiş
                // Buraya izin verildiyse yapılacak işlemleri ekle
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 2)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verilmiş
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            var secilenGorsel: Uri? = data.data
            try {
                context?.let {
                    if (secilenGorsel != null) {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source =
                                ImageDecoder.createSource(it.contentResolver, secilenGorsel)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            view?.findViewById<ImageView>(R.id.imageView2)
                                ?.setImageBitmap(secilenBitmap)
                        } else {
                            secilenBitmap =
                                MediaStore.Images.Media.getBitmap(it.contentResolver, secilenGorsel)
                            view?.findViewById<ImageView>(R.id.imageView2)
                                ?.setImageBitmap(secilenBitmap)
                        }

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun gorselboyutunuduzenle(kullaniciBitmap: Bitmap, maximumBoyut: Int): Bitmap {
        var width = kullaniciBitmap.width
        var height = kullaniciBitmap.height
        val bitmapOrani: Double = width.toDouble() / height.toDouble()
        if (bitmapOrani > 1) {
            width = maximumBoyut
            val kisaltilmisHeight = width / bitmapOrani
            height = kisaltilmisHeight.toInt()
        } else {
            height = maximumBoyut
            val kisaltilmisWidth = height * bitmapOrani
            width = kisaltilmisWidth.toInt()
        }
        return Bitmap.createScaledBitmap(kullaniciBitmap, width, height, true)
    }


}