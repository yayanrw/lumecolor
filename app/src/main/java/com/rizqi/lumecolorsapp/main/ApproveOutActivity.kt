package com.rizqi.lumecolorsapp.main

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rizqi.lumecolorsapp.R
import com.rizqi.lumecolorsapp.adapter.ApproveOutAdapter
import com.rizqi.lumecolorsapp.adapter.TabQRList
import com.rizqi.lumecolorsapp.api.GetDataService
import com.rizqi.lumecolorsapp.api.RetrofitClients
import com.rizqi.lumecolorsapp.model.MApprove
import com.rizqi.lumecolorsapp.model.MTabQR
import com.rizqi.lumecolorsapp.response.*
import com.rizqi.lumecolorsapp.utils.Constants
import com.rizqi.lumecolorsapp.utils.Constants.LOADING_MSG
import com.rizqi.lumecolorsapp.utils.Constants.SP_LEVEL
import com.rizqi.lumecolorsapp.utils.SharedPreferencesUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class ApproveOutActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferencesUtils
    private lateinit var _SPLEVEL: String

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var mAdapter: ApproveOutAdapter
    private lateinit var mAdapterQR: TabQRList
    private lateinit var datePicker: DatePickerDialog
    private lateinit var mLoading: ProgressDialog

    //    Variable From Layout
    private lateinit var emptyState: TextView
    private lateinit var emptyStateQR: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var listQRShow: RecyclerView
    private lateinit var textDateFrom: TextView
    private lateinit var textDateTo: TextView
    private lateinit var imgDateFrom: ImageView
    private lateinit var imgDateTo: ImageView
    private lateinit var lnrChooseQr: LinearLayout
    private lateinit var lytQr: RelativeLayout
    private lateinit var vBack: LinearLayout
    private lateinit var vBackQR: LinearLayout
    private lateinit var lnrImageShow: LinearLayout
    private lateinit var mImgShow: ImageView
    private lateinit var btnAlamat: LinearLayout
    private lateinit var lnrAlamatView: LinearLayout
    private lateinit var lytAlamat: RelativeLayout
    private lateinit var mImageSearch: ImageView
    private lateinit var lnrSearchView: LinearLayout
    private lateinit var etSearch: EditText
    private lateinit var rlHeader: RelativeLayout
    private lateinit var btnApprove: RelativeLayout
    private lateinit var spinnerProduk: Spinner
    private lateinit var spinnerQR: Spinner
    private lateinit var spinnerQRSampai: Spinner
    private lateinit var btnAdd: LinearLayout
    private lateinit var llEmpty: LinearLayout
    private lateinit var btnUlangi: Button

    private lateinit var itemList: ArrayList<MApprove>
    private lateinit var searchItem: ArrayList<MApprove>
    private lateinit var qrCodeProduk: String
    private lateinit var qrCodeSampai: String
    private lateinit var idProduk: String

    var isDetail: Boolean = false
    var isImgShow: Boolean = false
    var isSearch: Boolean = false
    var isAlamatShow: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approve_out)

        sharedPreferences = SharedPreferencesUtils(this@ApproveOutActivity)
        _SPLEVEL = sharedPreferences.getStringSharedPreferences(SP_LEVEL)!!

        mLoading = ProgressDialog(this@ApproveOutActivity)
        mLoading.setCancelable(false)

//        Variable From Layout
        emptyState = findViewById(R.id.empty_state)
        emptyStateQR = findViewById(R.id.empty_state_qr)
        recyclerView = findViewById(R.id.rv_show)
        listQRShow = findViewById(R.id.list_qr_terpilih)
        textDateFrom = findViewById(R.id.txt_date_from)
        textDateTo = findViewById(R.id.txt_date_to)
        imgDateFrom = findViewById(R.id.img_date_from)
        imgDateTo = findViewById(R.id.img_date_to)
        lnrChooseQr = findViewById(R.id.layout_pilih_qr)
        lytQr = findViewById(R.id.layout_qr)
        vBack = findViewById(R.id.view_back)
        vBackQR = findViewById(R.id.view_back_qr)
        lnrImageShow = findViewById(R.id.linear_image_show)
        mImgShow = findViewById(R.id.image_show)
        btnAlamat = findViewById(R.id.button_alamat)
        lnrAlamatView = findViewById(R.id.layout_alamat_view)
        lytAlamat = findViewById(R.id.layout_alamat)
        mImageSearch = findViewById(R.id.logo_search)
        lnrSearchView = findViewById(R.id.search_view)
        etSearch = findViewById(R.id.edit_text_search)
        rlHeader = findViewById(R.id.header_title)
        btnApprove = findViewById(R.id.button_approved)
        spinnerProduk = findViewById(R.id.spinner_produk)
        spinnerQR = findViewById(R.id.spinner_qr)
        spinnerQRSampai = findViewById(R.id.spinner_qr_sampai)
        btnAdd = findViewById(R.id.button_tambah)
        llEmpty = findViewById(R.id.ll_empty_state)
        btnUlangi = findViewById(R.id.button_ulangi)

//        buttonListQR = findViewById(R.id.button_list_qr)
//        buttonPacking = findViewById(R.id.button_packing)
//        buttonSender = findViewById(R.id.button_sender)
//        buttonApproved = findViewById(R.id.button_approved)

        itemList = ArrayList()
        searchItem = ArrayList()
        qrCodeProduk = ""
        qrCodeSampai = ""
        idProduk = ""

        isDetail = false
        isImgShow = false
        isSearch = false
        isAlamatShow = false

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        var day = c.get(Calendar.DAY_OF_MONTH)

        var fixMonth: String = (month + 1).toString()
        var fixDay: String = day.toString()

        if (fixMonth.length == 1) {
            fixMonth = "0$fixMonth"
        }
        if (fixDay.length == 1) {
            fixDay = "0$fixDay"
        }

        val dateNow = "${year}-${fixMonth}-${fixDay}"

        textDateFrom.text = dateNow
        textDateTo.text = dateNow

        setOnClickHandler()

        getListHistory(dateNow, dateNow)

        setDateRange(day, month, year)

        searchAction()

    }

    private fun setSpinnerProduk(order_id: String) {
        mLoading.setMessage(LOADING_MSG)
        mLoading.show()

        val service = RetrofitClients().getRetrofitInstance().create(GetDataService::class.java)
        val call = service.listProduk(order_id)

        call.enqueue(object : Callback<ResponseProduk> {

            override fun onFailure(call: Call<ResponseProduk>, t: Throwable) {

                Toast.makeText(
                    this@ApproveOutActivity,
                    "Something went wrong...Please try later!",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d("FAILED :", t.message.toString())
                mLoading.dismiss()
            }

            override fun onResponse(
                call: Call<ResponseProduk>,
                response: Response<ResponseProduk>
            ) {

                val res = response.body()!!

                if (res.status == Constants.STAT200) {

                    idProduk = res.data[0].id

                    val listOfItems = ArrayList<String>()

                    (0 until res.data.size).forEach { position ->
                        listOfItems.add(res.data[position].nama_produk)
                    }

                    val spinnerAdapter =
                        ArrayAdapter(this@ApproveOutActivity, R.layout.item_spinner, listOfItems)
                    spinnerAdapter.setDropDownViewResource(R.layout.item_spinner)
                    spinnerProduk.adapter = spinnerAdapter

                    spinnerProduk.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                idProduk = res.data[position].id
                                setSpinnerQR(res.data[position].id)
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {

                            }

                        }

                } else {

                    Toast.makeText(
                        this@ApproveOutActivity,
                        res.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                mLoading.dismiss()

            }

        })
    }

    private fun setSpinnerQR(produkId: String) {
//        mLoading.setMessage(LOADING_MSG)
//        mLoading.show()

        val service = RetrofitClients().getRetrofitInstance().create(GetDataService::class.java)
        val call = service.QRByProduk(produkId)

        call.enqueue(object : Callback<ResponseListQR> {

            override fun onFailure(call: Call<ResponseListQR>, t: Throwable) {

                Toast.makeText(
                    this@ApproveOutActivity,
                    "Something went wrong...Please try later!",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d("FAILED :", t.message.toString())
                mLoading.dismiss()
            }

            override fun onResponse(
                call: Call<ResponseListQR>,
                response: Response<ResponseListQR>
            ) {

                val res = response.body()!!

                if (res.status == Constants.STAT200) {

                    if (res.data.size != 0) {

                        val listOfItems = ArrayList<String>()

                        (0 until res.data.size).forEach { position ->
                            listOfItems.add(res.data[position].id)
                        }

                        qrCodeProduk = listOfItems[0]
                        setSpinnerQRSampai(idProduk, qrCodeProduk)

                        val spinnerAdapter = ArrayAdapter(
                            this@ApproveOutActivity,
                            R.layout.item_spinner,
                            listOfItems
                        )
                        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner)
                        spinnerQR.adapter = spinnerAdapter
                        spinnerQR.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    qrCodeProduk = listOfItems[position]
                                    setSpinnerQRSampai(idProduk, qrCodeProduk)
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }

                            }
                    } else {
                        Toast.makeText(
                            this@ApproveOutActivity,
                            "Tidak ada QR Code dari Produk ini.",
                            Toast.LENGTH_LONG
                        ).show()

                        qrCodeProduk = ""

                        val listOfItems = ArrayList<String>()

                        val spinnerAdapter = ArrayAdapter(
                            this@ApproveOutActivity,
                            R.layout.item_spinner,
                            listOfItems
                        )
                        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner)
                        spinnerQR.adapter = spinnerAdapter
                    }

                } else {

                    Toast.makeText(
                        this@ApproveOutActivity,
                        res.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                mLoading.dismiss()

            }

        })
    }

    private fun setSpinnerQRSampai(produkId: String, qrDari: String) {
//        mLoading.setMessage(LOADING_MSG)
//        mLoading.show()

        val service = RetrofitClients().getRetrofitInstance().create(GetDataService::class.java)
        val call = service.QRByProdukSampai(produkId, qrDari)

        call.enqueue(object : Callback<ResponseListQRSampai> {

            override fun onFailure(call: Call<ResponseListQRSampai>, t: Throwable) {

                Toast.makeText(
                    this@ApproveOutActivity,
                    "Something went wrong...Please try later!",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d("FAILED :", t.message.toString())
                mLoading.dismiss()
            }

            override fun onResponse(
                call: Call<ResponseListQRSampai>,
                response: Response<ResponseListQRSampai>
            ) {

                val res = response.body()!!

                if (res.status == Constants.STAT200) {

                    if (res.data.size != 0) {
//                        qrCodeSampai = res.data[0].id

                        val listOfItems = ArrayList<String>()

                        (0 until res.data.size).forEach { position ->
                            if (qrDari != res.data[position].id) {
                                listOfItems.add(res.data[position].id)
                            }
                        }

                        qrCodeSampai = listOfItems[0]

                        val spinnerAdapter = ArrayAdapter(
                            this@ApproveOutActivity,
                            R.layout.item_spinner,
                            listOfItems
                        )
                        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner)
                        spinnerQRSampai.adapter = spinnerAdapter
                        spinnerQRSampai.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    qrCodeSampai = listOfItems[position]
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }

                            }
                    } else {
                        Toast.makeText(
                            this@ApproveOutActivity,
                            "Tidak ada QR Code dari Produk ini.",
                            Toast.LENGTH_LONG
                        ).show()

                        qrCodeSampai = ""

                        val listOfItems = ArrayList<String>()

                        val spinnerAdapter = ArrayAdapter(
                            this@ApproveOutActivity,
                            R.layout.item_spinner,
                            listOfItems
                        )
                        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner)
                        spinnerQRSampai.adapter = spinnerAdapter
                    }

                } else {

                    Toast.makeText(
                        this@ApproveOutActivity,
                        res.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                mLoading.dismiss()

            }

        })
    }

    private fun addQR(data: MApprove) {
        if (idProduk != "") {
            mLoading.setMessage(LOADING_MSG)
            mLoading.show()

            val service = RetrofitClients().getRetrofitInstance().create(GetDataService::class.java)
            val call = service.saveQR(data.order_id, idProduk, qrCodeProduk, qrCodeSampai)

            call.enqueue(object : Callback<ResponseApprove> {

                override fun onFailure(call: Call<ResponseApprove>, t: Throwable) {

                    Toast.makeText(
                        this@ApproveOutActivity,
                        "Something went wrong...Please try later!",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.d("FAILED :", t.message.toString())
                    mLoading.dismiss()
                }

                override fun onResponse(
                    call: Call<ResponseApprove>,
                    response: Response<ResponseApprove>
                ) {

                    val res = response.body()!!

                    if (res.status == Constants.STAT200) {

                        Toast.makeText(
                            this@ApproveOutActivity,
                            "Berhasil Menambahkan QR Code.",
                            Toast.LENGTH_LONG
                        ).show()

                        fetchListQR(data)

                    } else {

                        Toast.makeText(
                            this@ApproveOutActivity,
                            res.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    mLoading.dismiss()

                }

            })
        } else {
            Toast.makeText(
                this@ApproveOutActivity,
                "Tidak ada QR Code yang Terpilih.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun removeQR(data: MApprove, qr: String) {
        mLoading.setMessage(LOADING_MSG)
        mLoading.show()

        val service = RetrofitClients().getRetrofitInstance().create(GetDataService::class.java)
        val call = service.removeQR(qr)

        call.enqueue(object : Callback<ResponseDeleteQR> {

            override fun onFailure(call: Call<ResponseDeleteQR>, t: Throwable) {

                Toast.makeText(
                    this@ApproveOutActivity,
                    "Something went wrong...Please try later!",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d("FAILED :", t.message.toString())
                mLoading.dismiss()
            }

            override fun onResponse(
                call: Call<ResponseDeleteQR>,
                response: Response<ResponseDeleteQR>
            ) {

                val res = response.body()!!

                if (res.status == Constants.STAT200) {

                    fetchListQR(data)

                } else {

                    Toast.makeText(
                        this@ApproveOutActivity,
                        res.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                mLoading.dismiss()

            }

        })
    }

    private fun showDialogApprove(order_id: String) {
        val dialog = Dialog(this@ApproveOutActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_pilih_qr)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnNo = dialog.findViewById<Button>(R.id.button_no)
        val btnYes = dialog.findViewById<Button>(R.id.button_yes)

        btnNo.setOnClickListener { dialog.dismiss() }
        btnYes.setOnClickListener {
            dialog.dismiss()
            fetchApproveOut(order_id)
        }

        dialog.show()
    }

    private fun showDialogApprovePacking(order_id: String) {
        val dialog = Dialog(this@ApproveOutActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_packing)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnNo = dialog.findViewById<Button>(R.id.button_no)
        val btnYes = dialog.findViewById<Button>(R.id.button_yes)

        btnNo.setOnClickListener { dialog.dismiss() }
        btnYes.setOnClickListener {
            dialog.dismiss()
            fetchApprovePacking(order_id)
        }

        dialog.show()
    }

    private fun showDialogApproveSender(order_id: String) {
        val dialog = Dialog(this@ApproveOutActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_sender)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnNo = dialog.findViewById<Button>(R.id.button_no)
        val btnYes = dialog.findViewById<Button>(R.id.button_yes)

        btnNo.setOnClickListener { dialog.dismiss() }
        btnYes.setOnClickListener {
            dialog.dismiss()
            fetchApproveSender(order_id)
        }

        dialog.show()
    }

    private fun setOnClickHandler() {

        mImageSearch.setOnClickListener {
            showSearch(true)
        }

        btnAlamat.setOnClickListener {
            lnrAlamatView.visibility = View.VISIBLE
            isAlamatShow = true
        }

        vBack.setOnClickListener {
            lnrAlamatView.visibility = View.GONE
            isAlamatShow = false
        }

        lnrAlamatView.setOnClickListener {
            lnrAlamatView.visibility = View.GONE
            isAlamatShow = false
        }

        lnrChooseQr.setOnClickListener {
            lnrChooseQr.visibility = View.GONE
            isDetail = false
        }

        btnUlangi.setOnClickListener {
            getListHistory(textDateFrom.text.toString(), textDateTo.text.toString())
        }

        lytQr.setOnClickListener { }

        lytAlamat.setOnClickListener { }
    }

    private fun fetchApproveOut(order_id: String) {
        mLoading.setMessage(LOADING_MSG)
        mLoading.show()

        val service = RetrofitClients().getRetrofitInstance().create(GetDataService::class.java)
        val call = service.approveOut(order_id, _SPLEVEL)

        call.enqueue(object : Callback<ResponseApprove> {

            override fun onFailure(call: Call<ResponseApprove>, t: Throwable) {

                Toast.makeText(
                    this@ApproveOutActivity,
                    "Something went wrong...Please try later!",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d("FAILED :", t.message.toString())

                lnrChooseQr.visibility = View.GONE
                isDetail = false
                getListHistory(textDateFrom.text.toString(), textDateTo.text.toString())
            }

            override fun onResponse(
                call: Call<ResponseApprove>,
                response: Response<ResponseApprove>
            ) {

                val res = response.body()!!

                if (res.status == Constants.STAT200) {

                    Toast.makeText(
                        this@ApproveOutActivity,
                        "Data berhasil di proses",
                        Toast.LENGTH_LONG
                    ).show()

                } else {

                    Toast.makeText(
                        this@ApproveOutActivity,
                        res.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                lnrChooseQr.visibility = View.GONE
                isDetail = false
                getListHistory(textDateFrom.text.toString(), textDateTo.text.toString())
            }

        })
    }

    private fun fetchApprovePacking(order_id: String) {
        mLoading.setMessage(LOADING_MSG)
        mLoading.show()

        val service = RetrofitClients().getRetrofitInstance().create(GetDataService::class.java)
        val call = service.approvePacking(order_id, 1)

        call.enqueue(object : Callback<ResponseApprove> {

            override fun onFailure(call: Call<ResponseApprove>, t: Throwable) {

                Toast.makeText(
                    this@ApproveOutActivity,
                    "Something went wrong...Please try later!",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d("FAILED :", t.message.toString())

                lnrChooseQr.visibility = View.GONE
                isDetail = false
                getListHistory(textDateFrom.text.toString(), textDateTo.text.toString())
            }

            override fun onResponse(
                call: Call<ResponseApprove>,
                response: Response<ResponseApprove>
            ) {

                val res = response.body()!!

                if (res.status == Constants.STAT200) {

                    Toast.makeText(
                        this@ApproveOutActivity,
                        "Data berhasil di proses",
                        Toast.LENGTH_LONG
                    ).show()

                } else {

                    Toast.makeText(
                        this@ApproveOutActivity,
                        res.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                lnrChooseQr.visibility = View.GONE
                isDetail = false
                getListHistory(textDateFrom.text.toString(), textDateTo.text.toString())
            }

        })
    }

    private fun fetchApproveSender(order_id: String) {
        mLoading.setMessage(LOADING_MSG)
        mLoading.show()

        val service = RetrofitClients().getRetrofitInstance().create(GetDataService::class.java)
        val call = service.approveSender(order_id, 1)

        call.enqueue(object : Callback<ResponseApprove> {

            override fun onFailure(call: Call<ResponseApprove>, t: Throwable) {

                Toast.makeText(
                    this@ApproveOutActivity,
                    "Something went wrong...Please try later!",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d("FAILED :", t.message.toString())

                lnrChooseQr.visibility = View.GONE
                isDetail = false
                getListHistory(textDateFrom.text.toString(), textDateTo.text.toString())
            }

            override fun onResponse(
                call: Call<ResponseApprove>,
                response: Response<ResponseApprove>
            ) {

                val res = response.body()!!

                if (res.status == Constants.STAT200) {

                    Toast.makeText(
                        this@ApproveOutActivity,
                        "Data berhasil di proses",
                        Toast.LENGTH_LONG
                    ).show()

                } else {

                    Toast.makeText(
                        this@ApproveOutActivity,
                        res.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                lnrChooseQr.visibility = View.GONE
                isDetail = false
                getListHistory(textDateFrom.text.toString(), textDateTo.text.toString())
            }

        })
    }

    private fun getListHistory(dari: String, sampai: String) {
        mLoading.setMessage(LOADING_MSG)
        mLoading.show()

        llEmpty.visibility = View.GONE
        recyclerView.visibility = View.GONE
        btnUlangi.visibility = View.GONE

        val service = RetrofitClients().getRetrofitInstance().create(GetDataService::class.java)
        val call = service.approveOutHistory(dari, sampai)

        call.enqueue(object : Callback<ResponseApprove> {

            override fun onFailure(call: Call<ResponseApprove>, t: Throwable) {

                Toast.makeText(
                    this@ApproveOutActivity,
                    t.message.toString(),
                    Toast.LENGTH_SHORT
                ).show()

                Log.d("FAILEDLOAD:", t.message.toString())

                llEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                btnUlangi.visibility = View.VISIBLE

                if (t.message.toString() == "timeout") emptyState.text =
                    "Timed Out, Ulangi memuat data."
                else emptyState.text = "Terjadi kesalahan saat memuat data."

                mLoading.dismiss()
            }

            override fun onResponse(
                call: Call<ResponseApprove>,
                response: Response<ResponseApprove>
            ) {

                val res = response.body()!!

                if (res.status == Constants.STAT200) {

                    itemList = res.data

                    if (res.data.size != 0) {
                        llEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        btnUlangi.visibility = View.GONE
//                        Log.d("MERCHANT: ", res.data[0].nama_vendor)
                        setRecyclerView(res.data)
                    } else {
                        llEmpty.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        btnUlangi.visibility = View.GONE
                        emptyState.text = "Tidak ada data pada jangka waktu ini."
                    }

//                    Log.d("DATASIZE: ", "${res.data.size}")

                } else {

                    Toast.makeText(
                        this@ApproveOutActivity,
                        "GAGAL",
                        Toast.LENGTH_LONG
                    ).show()

                    llEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    btnUlangi.visibility = View.VISIBLE
                    emptyState.text = res.message
                }

                mLoading.dismiss()
            }

        })
    }

    private fun setRecyclerView(data: ArrayList<MApprove>) {
        linearLayoutManager = LinearLayoutManager(this@ApproveOutActivity)
        mAdapter = ApproveOutAdapter(data, this@ApproveOutActivity)
        recyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = mAdapter
        }

        vBackQR.setOnClickListener {
            lnrChooseQr.visibility = View.GONE
            isDetail = false
        }

        mAdapter.interfaAction(object : ApproveOutAdapter.InterfaceAdapter {
            override fun onBtnClick(data: MApprove) {
                lnrChooseQr.visibility = View.VISIBLE
                isDetail = true
                fetchListQR(data)

                btnApprove.setOnClickListener {
                    showDialogApprove(data.order_id)
                }

                btnAdd.setOnClickListener {
                    addQR(data)
                }

//                setSpinnerProduk(data.order_id)
            }

            override fun onApprovePacking(data: MApprove) {
                showDialogApprovePacking(data.order_id)
            }

            override fun onApproveSender(data: MApprove) {
                showDialogApproveSender(data.order_id)
            }

            override fun onBtnClickImage(data: MApprove) {
                lnrImageShow.visibility = View.VISIBLE
                isImgShow = true
            }

        })
    }

    private fun searchAction() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (isSearch && etSearch.text.isNotEmpty()) {
                    searchItem = ArrayList()

                    for (i in 0 until itemList.size) {
                        val item = itemList[i]
                        if (item.nama_vendor.contains(etSearch.text, ignoreCase = true)) {
                            searchItem.add(item)
                        }
                    }

                    if (searchItem.size != 0) {
                        llEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        btnUlangi.visibility = View.GONE

                        setRecyclerView(searchItem)
                    } else {
                        llEmpty.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        btnUlangi.visibility = View.GONE

                        if (itemList.size == 0) emptyState.text =
                            "Tidak ada data pada jangka waktu ini."
                        else emptyState.text = "Barang tidak ditemukan."
                    }
                } else if (isSearch && etSearch.text.isEmpty()) {
                    setRecyclerView(itemList)
                    if (itemList.size == 0) {
                        llEmpty.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        btnUlangi.visibility = View.GONE

                        emptyState.text = "Tidak ada data pada jangka waktu ini."
                    } else {
                        llEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        btnUlangi.visibility = View.GONE
                        emptyState.text = ""
                    }
                }
            }

        })
    }

    private fun fetchListQR(data: MApprove) {
        emptyStateQR.visibility = View.VISIBLE
        listQRShow.visibility = View.GONE
        emptyStateQR.text = "Memuat..."

        val service = RetrofitClients().getRetrofitInstance().create(GetDataService::class.java)
        val call = service.tabQR(data.order_id)

        call.enqueue(object : Callback<ResponseTabQR> {

            override fun onFailure(call: Call<ResponseTabQR>, t: Throwable) {

                Toast.makeText(
                    this@ApproveOutActivity,
                    "Something went wrong...Please try later!",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d("FAILED :", t.message.toString())

                emptyStateQR.visibility = View.VISIBLE
                listQRShow.visibility = View.GONE
                emptyStateQR.text = "Terjadi kesalahan saat memuat data."
            }

            override fun onResponse(call: Call<ResponseTabQR>, response: Response<ResponseTabQR>) {

                val res = response.body()!!

                if (res.status == Constants.STAT200) {
                    if (res.data.size != 0) {
                        emptyStateQR.visibility = View.GONE
                        listQRShow.visibility = View.VISIBLE

                        setRecyclerQR(res.data, data)

                    } else {
                        emptyStateQR.visibility = View.VISIBLE
                        listQRShow.visibility = View.GONE
                        emptyStateQR.text = "Tidak ada data."
                    }

                    setSpinnerProduk(data.order_id)

                } else {

                    Toast.makeText(
                        this@ApproveOutActivity,
                        "GAGAL",
                        Toast.LENGTH_LONG
                    ).show()

                    emptyStateQR.visibility = View.VISIBLE
                    listQRShow.visibility = View.GONE
                    emptyStateQR.text = "${res.message}"
                }
            }

        })
    }

    private fun setRecyclerQR(data: ArrayList<MTabQR>, dataApprove: MApprove) {
        linearLayoutManager = LinearLayoutManager(this@ApproveOutActivity)
        mAdapterQR = TabQRList(data, this@ApproveOutActivity)
        listQRShow.apply {
            layoutManager = linearLayoutManager
            adapter = mAdapterQR
        }

        mAdapterQR.interfaAction(object : TabQRList.InterfaceAdapter {
            override fun onDelete(data: MTabQR) {
                removeQR(dataApprove, data.qrcode)
            }

        })
    }

    private fun setDateRange(day: Int, month: Int, year: Int) {
        imgDateFrom.setOnClickListener {
            datePicker = DatePickerDialog(this@ApproveOutActivity,
                { view, year, month, dayOfMonth ->
                    var fixMonth: String = (month + 1).toString()
                    var fixDay: String = dayOfMonth.toString()

                    if (fixMonth.length == 1) {
                        fixMonth = "0$fixMonth"
                    }
                    if (fixDay.length == 1) {
                        fixDay = "0$fixDay"
                    }

                    textDateFrom.text = "${year}-${fixMonth}-${fixDay}"
                }, year, month, day
            )
            datePicker.show()
        }

        textDateFrom.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                Do Something
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                Do Something
            }

            override fun afterTextChanged(s: Editable?) {
                showSearch(false)
                getListHistory(textDateFrom.text.toString(), textDateTo.text.toString())
            }

        })

        imgDateTo.setOnClickListener {
            datePicker = DatePickerDialog(this@ApproveOutActivity,
                { view, year, month, dayOfMonth ->

                    var fixMonth: String = (month + 1).toString()
                    var fixDay: String = dayOfMonth.toString()

                    if (fixMonth.length == 1) {
                        fixMonth = "0$fixMonth"
                    }
                    if (fixDay.length == 1) {
                        fixDay = "0$fixDay"
                    }

                    textDateTo.text = "${year}-${fixMonth}-${fixDay}"
                }, year, month, day
            )
            datePicker.show()
        }

        textDateTo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                Do Something
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                Do Something
            }

            override fun afterTextChanged(s: Editable?) {
                showSearch(false)
                getListHistory(textDateFrom.text.toString(), textDateTo.text.toString())
            }

        })
    }

    private fun showSearch(show: Boolean) {
        if (show) {
            isSearch = true
            lnrSearchView.visibility = View.VISIBLE
            rlHeader.visibility = View.GONE
        } else {
            isSearch = false
            lnrSearchView.visibility = View.GONE
            rlHeader.visibility = View.VISIBLE
        }
        etSearch.setText("")
    }

    override fun onBackPressed() {
        if (isImgShow) {
            isImgShow = false
            lnrImageShow.visibility = View.GONE
            return
        }
        if (isDetail) {
            isDetail = false
            lnrChooseQr.visibility = View.GONE
            return
        }
        if (isAlamatShow) {
            isAlamatShow = false
            lnrAlamatView.visibility = View.GONE
            return
        }
        if (isSearch) {
            showSearch(false)
            if (itemList.size != 0) {
                llEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                btnUlangi.visibility = View.GONE

                setRecyclerView(itemList)
            } else {
                llEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                btnUlangi.visibility = View.GONE

                emptyState.text = "Tidak ada data pada jangka waktu ini."
            }
            return
        }
        super.onBackPressed()
    }

}