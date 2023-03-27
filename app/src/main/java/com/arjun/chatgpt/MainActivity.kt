package com.arjun.chatgpt

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.arjun.chatgpt.model.Message
import com.arjun.chatgpt.model.MessageAdapter
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    lateinit var queryEdt : TextInputEditText
    lateinit var welcome : TextView
    lateinit var sendBtn : ImageView
    lateinit var messageRV : RecyclerView
    lateinit var progressDialog : ProgressDialog

    lateinit var messageAdapter : MessageAdapter
    lateinit var messageList : ArrayList<Message>
    val url = "https://api.openai.com/v1/completions"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        queryEdt = findViewById(R.id.message_edit_text)
        messageRV = findViewById(R.id.recycler_view)
        welcome = findViewById(R.id.welcome_text)
        sendBtn = findViewById(R.id.send_btn)

        messageList = ArrayList()
        val lm = LinearLayoutManager(applicationContext)
        lm.stackFromEnd = true
        messageRV.layoutManager = lm
        messageRV.setHasFixedSize(true)

        sendBtn.setOnClickListener {
            progressDialog = ProgressDialog(this@MainActivity)
            progressDialog.setTitle("Loading ChatGPT")
            progressDialog.setMessage("ChatGPT is loading, please wait")
            progressDialog.show()
            welcome.visibility = View.GONE
            if (queryEdt.text.toString().length > 0) {
                messageList.add(Message(queryEdt.text.toString(), "user"))
                messageAdapter = MessageAdapter(messageList)
                messageRV.adapter = messageAdapter
                messageAdapter.notifyDataSetChanged()
                getResponse(queryEdt.text.toString())
            } else {
                Toast.makeText(applicationContext, "Please Enter Data", Toast.LENGTH_SHORT)
                    .show()
            }
        }


   /*     queryEdt.setOnEditorActionListener(TextView.OnEditorActionListener { textView: TextView?, i: Int, keyEvent: KeyEvent? ->
            if (i == EditorInfo.IME_ACTION_SEND) {

                return@OnEditorActionListener true
            }
            false
        })*/
    }

    private fun getResponse(query: String) {
        queryEdt.setText("")
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        val jsonObject: JSONObject = JSONObject()
        jsonObject.put("model", "text-davinci-003")
        jsonObject.put("prompt", query)
        jsonObject.put("temperature", 0)
        jsonObject.put("top_p", 1)
        jsonObject.put("max_tokens", 100)

        val postRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST, url, jsonObject,
            Response.Listener { response ->
                val responseMsg: String =
                    response.getJSONArray("choices").getJSONObject(0).getString("text")
                messageList.add(Message(responseMsg, "bot"))
                messageAdapter = MessageAdapter(messageList)
                messageRV.adapter = messageAdapter
                messageAdapter.notifyDataSetChanged()

                progressDialog.dismiss()

            }, Response.ErrorListener {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/json"
                params["Authorization"] = "Bearer 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"
                return params
            }
        }

        postRequest.setRetryPolicy(object : RetryPolicy{
            override fun getCurrentTimeout(): Int {
                return 5000
            }

            override fun getCurrentRetryCount(): Int {
                return 5000
            }

            override fun retry(error: VolleyError?) {

            }
        })
        queue.add(postRequest)
    }
}