package org.openmrs.arden;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Locale;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.openmrs.ConceptWord;
import org.openmrs.api.context.Context;
import org.openmrs.Patient;
import org.openmrs.arden.*;


public class MLMObject {
	
	private HashMap<String, MLMObjectElement> conceptMap ;
	private String ConceptVar;
	private boolean IsVarAdded;
	private Context context;
	private Locale locale;
	private Patient patient;
	private LinkedList<String> ifList;
	private HashMap<String, String> userVarMapFinal ;
	private String className;

//	private Iterator<String> iter; 
	
	// default constructor
	public MLMObject(){
		conceptMap = new HashMap <String, MLMObjectElement>();
		IsVarAdded = false;
		userVarMapFinal = new HashMap <String, String>();
	}
	
	public MLMObject(Context c, Locale l, Patient p)
	{
		conceptMap = new HashMap <String, MLMObjectElement>();
		IsVarAdded = false;
		context = c;
		locale = l;
		patient = p;
		ifList = new LinkedList <String>();
		userVarMapFinal = new HashMap <String, String>();
	}

	public void SetContext(Context c) {
		context = c;
	}

	public void SetLocale(Locale l) {
		locale = l;
	}

	public void SetPatient(Patient p) {
		patient = p;
	}

	public void AddConcept(String s)
	{
		if(IsVarAdded == true && !conceptMap.containsKey(ConceptVar)) {
			conceptMap.put(ConceptVar, new MLMObjectElement(s, "", ""));
			IsVarAdded = false;    // for next time
			ConceptVar = "";
		}
	}
	
	public void SetConceptVar(String s)
	{
		ConceptVar = s;
		IsVarAdded = true;
	}
	
	public void ResetConceptVar()
	{
		ConceptVar = "";
		IsVarAdded = false;
	}
	
	public void PrintConcept(String key)
	{
		System.out.println("__________________________________");
	     MLMObjectElement mo = conceptMap.get(key);
	     {
	       System.out.println(mo.getConceptName() + " = " + mo.getObsVal(locale) + 
	    		   "\n Answer = " + mo.getAnswer() + 
	    		   //"\n Operator = " + mo.getCompOp() +
	    		   "\n Conclude Val = " + mo.getConcludeVal() +
	    		   "\n User Vars = " + mo.getUserVarVal()
	       	);
	    System.out.println("__________________________________");
		    
	       
	     }
	}
	
	public void PrintConceptMap()
	{
	//	System.out.println("Concepts are - ");
	//	Set<String> keys = conceptMap.keySet();
	//	for(String key : keys) {
	//	     System.out.println(key);
	//	}
		System.out.println("__________________________________");
	     Collection<MLMObjectElement> collection = conceptMap.values();
	     for(MLMObjectElement mo : collection) {
	       System.out.println(mo.getConceptName() + " = " + mo.getObsVal(locale) + 
	    		   "\n Answer = " + mo.getAnswer() + 
	    		   //"\n Operator = " + mo.getCompOp() +
	    		   "\n Conclude Val = " + mo.getConcludeVal() +
	    		   "\n User Vars = " + mo.getUserVarVal()
	    		   );
	    System.out.println("__________________________________");
		    
	       
	     }
	}
	
	public void PrintEvaluateList(){
		System.out.println("\n Evaluate order list is  - ");
		ListIterator<String> thisList = ifList.listIterator(0);
		while (thisList.hasNext()){
		     System.out.println(thisList.next());
		}
	}
	
	public boolean Evaluate(){
		boolean retVal = false;
		String key;
		ListIterator<String> thisList = ifList.listIterator(0);
		while (thisList.hasNext()){
			key = thisList.next();
			if(RetrieveConcept(key)){
				PrintConcept(key);
				if(EvaluateConcept(key)){ 
					if(isConclude(key)) {
					  retVal = conclude(key);	
					  break;  // concluded true or false
					}
					else {
							// set all the user defined variables
						addUserVarValFinal(key);
					}
				}
			}
		}
		return retVal;
	}
	public int GetSize(){
		return conceptMap.size();
	}
	//public void InitIterator() {
	//	iter = conceptMap.keySet().iterator();
	//}
	//public String GetNextConceptVar(){
	//	if(iter.hasNext()) { 
	//		return iter.next();
	//	}
	//	else {
	//		return null;
	//	}
	//}
	
	public String GetConceptName(String key){
		if(conceptMap.containsKey(key)) {
			return conceptMap.get(key).getConceptName();
		}
		else {
			return null;
		}
				
	}
	
	private MLMObjectElement GetMLMObjectElement(String key) {
		if(conceptMap.containsKey(key)) {
			return conceptMap.get(key);
		}
		else {
			return null;
		}
				
	}
	public void InitForIf() {
		ResetConceptVar();
	}
	public boolean RetrieveConcept(String key) {
		
		//TODO check to see if user authenticated
		boolean retVal = false;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null ){
			mObjElem.setServicesContext(context.getConceptService(), context.getObsService());
			if(mObjElem.getDBAccessRequired()){
				retVal = mObjElem.getConceptForPatient(locale, patient);
			}
			else {
				retVal = true; // No DB access required like else or simply conclude
			}
		}
		return retVal;
	}
	
	public boolean EvaluateConcept(String key) {
		boolean retVal = false;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null && !mObjElem.isElementEvaluated()){
			retVal = mObjElem.evaluate();
		}
		return retVal;
	}
	
	public boolean Evaluated(String key){
		boolean retVal = false;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			retVal = mObjElem.getEvaluated();
		}
		return retVal;
	}
	
	public Iterator <String> iterator(){
		Iterator iter;
		return iter = conceptMap.keySet().iterator();
	}
	
	public void AddToEvaluateList(String key){
		ifList.add(key);
		SetConceptVar(key);
	}
	
	public void SetCompOperator(Integer op, String key) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			mObjElem.setCompOp(op);
		}
	}
	
	public void SetAnswer (String val, String key) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			mObjElem.setAnswer(val);
		}
	}
	
	public void SetAnswer (int val, String key) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			mObjElem.setAnswer(val);
		}
	}
	public void SetAnswer (boolean val, String key) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			mObjElem.setAnswer(val);
		}
	}
	
	public void SetConcludeVal (boolean val, String key){
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			mObjElem.setConcludeVal(val);
		}
	}
		
	public void SetUserVarVal (String var, String val, String key) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			mObjElem.addUserVarVal(var, val);
		}
	}
	
	public void SetDBAccess(boolean val, String key ) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			mObjElem.setDBAccessRequired(val);
		}
	}
	
	public boolean GetDBAccess(String key ) {
		boolean retVal = false;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			retVal = mObjElem.getDBAccessRequired();
		}
		return retVal;
	}
	
	public void addUserVarValFinal(String key) {
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			String var = "", val = "";
		    Iterator iter = mObjElem.iterator();
			while(iter.hasNext()) {
				var = (String) iter.next();
				val = mObjElem.getUserVarVal(var);
				if(!userVarMapFinal.containsKey(var)) {
					userVarMapFinal.put(var, val);
				}
				else
				{
					//TODO either an error or overwrite previous one
				}
			}
		}
	}
	public boolean isConclude(String key) {
		boolean retVal = false;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			retVal = mObjElem.isConclude();
		}
		return retVal;
	}
	
	public boolean conclude(String key) {
		boolean retVal = false;
		MLMObjectElement mObjElem = GetMLMObjectElement(key);
		if(mObjElem != null){
			retVal = mObjElem.conclude();
		}
		return retVal;
	}
	
	public String getUserVarVal(String key) {
		String retVal = "";
		if(userVarMapFinal.containsKey(key)) {
			retVal = userVarMapFinal.get(key);
		}
		else if(key.equals("firstname")) {
			retVal = patient.getPatientName().getGivenName();
		}
		return retVal;
	}
	
	public void setClassName(String name) {
		className = name;
	}
	public String getClassName() {
		return className;
	}
}
