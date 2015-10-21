package com.domotrix.android;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.domotrix.language.DOMOTRIXCommand;
import com.domotrix.language.DOMOTRIXRecognition;
import com.domotrix.language.ParseException;
import com.domotrix.language.TokenMgrError;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SpeechRecognition {
	private final static String TAG = "SpeechRecognition";
	private SpeechRecognizer sr;

	public final int SPEECHTOTEXT = 1;

	public boolean start(Activity activity, String prompt) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				
		// Getting an instance of PackageManager
		PackageManager pm = activity.getPackageManager();
				
		// Querying Package Manager
		List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
				
		if (activities.size() <= 0) {
			// No Activity found to handle the action ACTION_RECOGNIZE_SPEECH
			return false;
		}

		if (sr != null) {
			sr = SpeechRecognizer.createSpeechRecognizer(activity);
			sr.setRecognitionListener(new SpeechRecognitionListener());   			
		}

		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
		RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"com.domotrix.android");
		
		activity.startActivityForResult(intent, SPEECHTOTEXT);	
		//if (sr != null) sr.startListening(intent);

		return true;
	}

	public DOMOTRIXCommand recognize(int requestCode, Intent data) {
		if (data == null) return null;

		if (requestCode == SPEECHTOTEXT) {
			ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);	
			try { 
				if (text != null && text.size() > 0) {
					Log.d(TAG,"Processing :"+text.get(0));
					DOMOTRIXCommand cmd = DOMOTRIXRecognition.toCommand(deAccent(text.get(0))+".");
					return cmd;
				}
			} catch (ParseException e) {
				return null;
			} catch (TokenMgrError ee) {
				return null;
			}
		}
		return null;
	}

	private String deAccent(String str) {
	    String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
	    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	    return pattern.matcher(nfdNormalizedString).replaceAll("");
	} 

	class SpeechRecognitionListener implements RecognitionListener {
		public void onReadyForSpeech(Bundle params) {
			Log.d(TAG, "onReadyForSpeech");
		}
		public void onBeginningOfSpeech() {
			Log.d(TAG, "onBeginningOfSpeech");
		}
		public void onRmsChanged(float rmsdB) {
			Log.d(TAG, "onRmsChanged");
		}
		public void onBufferReceived(byte[] buffer) {
			Log.d(TAG, "onBufferReceived");
		}
		public void onEndOfSpeech() {
			Log.d(TAG, "onEndofSpeech");
		}
		public void onError(int error) {
			Log.d(TAG,  "error " +  error);
			//mText.setText("error " + error);
		}
		public void onResults(Bundle results) {
			String str = new String();
			Log.d(TAG, "onResults " + results);
			ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			for (int i = 0; i < data.size(); i++) {
				Log.d(TAG, "result " + data.get(i));
				str += data.get(i);
			}
			//mText.setText("results: "+String.valueOf(data.size()));        
		}
		public void onPartialResults(Bundle partialResults) {
			Log.d(TAG, "onPartialResults");
		}
		public void onEvent(int eventType, Bundle params) {
			Log.d(TAG, "onEvent " + eventType);
		}
   }	   
}
