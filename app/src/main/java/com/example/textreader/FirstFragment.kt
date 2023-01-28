package com.example.textreader

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.textreader.databinding.FragmentFirstBinding
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfDocumentContentParser
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.util.*


class FirstFragment : Fragment(), View.OnClickListener, TextToSpeech.OnInitListener {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var inputStream: InputStream
    private lateinit var textToSpeech: TextToSpeech
    private var text: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        textToSpeech = TextToSpeech(requireContext(), this)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fab.setOnClickListener(this)
        binding.readFab.setOnClickListener(this)
    }

    private val startActivityForPDF = registerForActivityResult(ActivityResultContracts.StartActivityForResult() ) { result ->
        when(result.resultCode){
            Activity.RESULT_OK -> {
                val data = result.data
                getData(data)
            }
            else -> {

            }
        }
    }

    private fun openFileManager(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForPDF.launch(intent)
    }

    private fun getData(data: Intent?){
        if (data != null){
            var uri = data.data
            if(uri != null){
                binding.noFileGroup.visibility = View.GONE
                binding.extractedTextGroup.visibility = View.VISIBLE
                extractPDF(uri)
            }
        }

    }

    private fun extractPDF(uri : Uri){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                inputStream = activity?.contentResolver?.openInputStream(uri)!!

            } catch (e: Exception){
                e.printStackTrace()
            }

            var contentText = ""
            var fullTextBuilder = StringBuilder()
            var reader: PdfReader


            try {
                reader = PdfReader(inputStream)
                val doc = PdfDocument(reader)
                val parser = PdfDocumentContentParser(doc)
                val noOfPage = doc.numberOfPages
                var strategy: ITextExtractionStrategy
                for (i in 1..noOfPage){
                    val page = doc.getPage(i)
                    contentText = PdfTextExtractor.getTextFromPage(page, SimpleTextExtractionStrategy())
                    fullTextBuilder.append(contentText)
                }
                reader.close()

                withContext(Dispatchers.Main){
                    text = fullTextBuilder.toString()
                    binding.fileContentEt.setText(fullTextBuilder)
                    binding.readFab.visibility = View.VISIBLE
                }
            }
            catch (e: IOException){

            }
        }
    }

    private fun readText(){
        textToSpeech.speak(binding.fileContentEt.text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onInit(status: Int) {
        if(status != TextToSpeech.SUCCESS){
            val results = textToSpeech.setLanguage(Locale.US)

            if(results == TextToSpeech.LANG_MISSING_DATA || results == TextToSpeech.LANG_NOT_SUPPORTED)
                Log.d("First FragLogs", "No Language found :(" )
        }
        else {
            Log.d("First FragLogs", "Text To Speech Failed to init :(" )
        }
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.fab -> {
                openFileManager()
            }
            R.id.read_fab -> {
                readText()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        if(textToSpeech != null){
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroyView()
    }
}