package com.example.fooddescription

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class FoodList : Fragment() {
    private var yemekListesi = ArrayList<String>()
    private lateinit var recyclerView: RecyclerView
    private var yemekIdListesi = ArrayList<Int>()
    private lateinit var listeadapter : ListeCyclerAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_food_list, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.myrecyclerView)
        listeadapter = ListeCyclerAdapter(yemekListesi,yemekIdListesi)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = listeadapter
        sqlVeriAlma()
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun sqlVeriAlma(){
        try {
            activity?.let {
                val database = it.openOrCreateDatabase("Yemekler",android.content.Context.MODE_PRIVATE,null)
                val cursor = database.rawQuery("SELECT * FROM yemekler",null)
                val yemekIsmiA = cursor.getColumnIndex("foodname")
                println(yemekIsmiA)
                val yemekIdIndex = cursor.getColumnIndex("id")
                yemekListesi.clear()
                yemekIdListesi.clear()

                while (cursor.moveToNext()){
                    println(cursor.getString(yemekIsmiA))
                    val list1: Boolean = yemekListesi.add(cursor.getString(yemekIsmiA))
                    println("Liste Elemanlari : $list1")
                    yemekIdListesi.add(cursor.getInt(yemekIdIndex))

                }
                listeadapter.notifyDataSetChanged()
                cursor.close()
            }
        }
        catch (e:Exception){
            e.printStackTrace()
        }

    }

}