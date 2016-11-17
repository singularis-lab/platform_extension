package com.singularis.android.platformextension;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.singularis.android.platformextension.annotation.Activity;
import com.singularis.android.platformextension.annotation.InjectOnClick;
import com.singularis.android.platformextension.annotation.InjectView;
import com.singularis.android.platformextension.annotation.InjectResource;

public abstract class SmartyFragmentActivity extends FragmentActivity
{
	protected ViewGroup _root;
	protected final TypefaceManager _typefaceManager;
	private final HashMap<View, View.OnClickListener> _clickListeners;
	
	protected SmartyFragmentActivity()
	{
		_typefaceManager = TypefaceManager.Get();
		_clickListeners = new HashMap<View, View.OnClickListener>();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		for(Entry<View, View.OnClickListener> item: _clickListeners.entrySet())
		{
			item.getKey().setOnClickListener(item.getValue());
		}
	}
	
	@Override
	public void onPause()
	{
		for(View item: _clickListeners.keySet())
		{
			item.setOnClickListener(null);
		}
		
		super.onPause();
	}

	protected final void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
				
		_typefaceManager.Initialize(this);

		final Class<? extends SmartyFragmentActivity> clazz = getClass();

		if(!clazz.isAnnotationPresent(Activity.class))
			throw new RuntimeException("Layout not set for injection activity");

		final Activity annotation = (Activity) clazz.getAnnotation(Activity.class);

		int layoutResourceId;
		if(annotation.Layout() != -1)
			layoutResourceId = annotation.Layout();		
		else
			layoutResourceId = this.getResources().getIdentifier(annotation.LayoutName(), "layout", this.getPackageName());
		
		setContentView(layoutResourceId);
		
		_root = (ViewGroup)findViewById(android.R.id.content);
		
		Inject();
		OnCreate(savedInstanceState);		
	}

	protected void OnCreate(Bundle savedInstanceState)
	{
	}

	protected void Inject()
	{
		InjectView();
		InjectResource();
		InjectOnClickHadnler();
	}

	private void InjectOnClickHadnler()
	{
		final Resources resources = getResources();
		final String packageName  = getPackageName();
		final Field[] fields 	  = GetField(getClass());
		
		for(Field field : fields)
		{
			try
			{
				if(!field.isAnnotationPresent(InjectOnClick.class))
					continue;
				
				final InjectOnClick annotation = field.getAnnotation(InjectOnClick.class);
				final String path 			   = annotation.ElementName();				
				final View view 			   = GetView(path, resources, packageName);
				final Method handler 		   = getClass().getMethod(annotation.MethodName(), new Class[]{View.class});
				
				if(view == null)
					Log.e("platformextension", String.format("Can not find view with id %s", path));
				if(handler == null)
					Log.e("platformextension", String.format("Can not find method %s", annotation.MethodName()));
				
				if(view != null && handler != null)
					_clickListeners.put(view, new View.OnClickListener() 
					{						
						@Override
						public void onClick(View v) 
						{ 
							try 
							{
								handler.invoke(SmartyFragmentActivity.this, view);
							} 
							catch (IllegalAccessException e) 
							{						
								e.printStackTrace();
							} 
							catch (IllegalArgumentException e) 
							{						
								e.printStackTrace();
							} 
							catch (InvocationTargetException e) 
							{
								e.printStackTrace();
							}
						}
					});				
				
			}			
			catch (NoSuchMethodException e) 
			{
				e.printStackTrace();
			} 
			catch (SecurityException e) 
			{			
				e.printStackTrace();
			}
		}
	}
		
	private void InjectView()
	{
		final Resources resources = getResources();
		final String packageName  = getPackageName();
		final Field[] fields 	  = GetField(getClass());

		for(Field field : fields)
		{
			try
			{
				if(!field.isAnnotationPresent(InjectView.class))
					continue;

				field.setAccessible(true);
				if(!field.isAccessible())
					continue;

				final InjectView annotation = field.getAnnotation(InjectView.class);
				View view = GetView(annotation.name(), resources, packageName);
					
				if(view == null)
					Log.e("paltformextension", String.format("Can not find view with id %s", annotation.name()));
				else
					field.set(this, view);
				
			}			
			catch (IllegalAccessException ignored)
			{
			}
			catch(IllegalArgumentException e)
			{
				final InjectView annotation = field.getAnnotation(InjectView.class);
				View view = GetView(annotation.name(), resources, packageName);
				
				Log.d("platformextension", String.format("Can not set value for field: %s (%s) = %s", annotation.name(), field.getDeclaringClass().toString(), view.getClass().toString()));
				e.printStackTrace();
			}
		}
	}

	private void InjectResource()
	{
		final Resources resources = getResources();
		final Field[] fields 	  = GetField(getClass());

		for(Field field : fields)
		{
			try
			{
				if(!field.isAnnotationPresent(InjectResource.class))
					continue;

				field.setAccessible(true);
				if(!field.isAccessible())
					continue;

				final InjectResource annotation = field.getAnnotation(InjectResource.class);

				int elementSystemId = annotation.id();
				if(elementSystemId == -1)
					elementSystemId = resources.getIdentifier(annotation.name(), null, null);

				Object value = null;

				switch(annotation.type())
				{
					case String:
						value = resources.getString(elementSystemId);
						break;

					case Color:
						value = resources.getColor(elementSystemId);
						break;

					case Drawable:
						value = resources.getDrawable(elementSystemId);
						break;
				}

				if(value != null)
					field.set(this, value);
			}
			catch (IllegalAccessException ignored)
			{
				ignored.printStackTrace();
			}
			catch (IllegalArgumentException ignored)
			{
				ignored.printStackTrace();
			}
		}
	}
	
	private View GetView(String path, Resources resources, String packageName)
	{
		String[] pathComponents = path.split("/");
		
		View view = null;
		for(String pathComponent : pathComponents)
		{
			int viewId = resources.getIdentifier(pathComponent, "id", packageName);
			if(view == null)
				view = _root.findViewById(viewId);
			else
				view = view.findViewById(viewId);
		}		
		
		return view;		
	}
	
	private Field[] GetField(Class<?> clazz)
	{
		Field[] fields = clazz.getDeclaredFields();

		if(getClass().getSuperclass() != null && getClass().getSuperclass() != clazz)
		{
			Field[] parentFields = GetField(getClass().getSuperclass());
			Field[] buffer = new Field[fields.length + parentFields.length];

			System.arraycopy(fields, 0, buffer, 0, fields.length);
			System.arraycopy(parentFields, 0, buffer, fields.length, parentFields.length);

			fields = buffer;
		}

		return fields;
	}
}
