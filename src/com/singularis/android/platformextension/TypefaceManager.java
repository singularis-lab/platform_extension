package com.singularis.android.platformextension;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Typeface;

public class TypefaceManager 
{
	private static TypefaceManager _instance;
	private Context _context;
	private final HashMap<String, Typeface> _cache;
	
	protected TypefaceManager()
	{	
		_cache = new HashMap<String, Typeface>();
	}
	
	public static TypefaceManager Get()
	{
		if(_instance == null)
			_instance = new TypefaceManager();
		
		return _instance;
	}
	
	public void Initialize(Context context)
	{
		_context = context;
	}
	
	public Typeface GetTypeface(String name)
	{
		if(_cache.containsKey(name))
			return _cache.get(name);
		
		Typeface tf = Typeface.createFromAsset(_context.getAssets(), name);
		
		_cache.put(name, tf);
		
		return tf;
	}
}
