package cn.creable.surveyOnUCMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.jeo.vector.Feature;
import org.jeo.vector.Field;

import cn.creable.ucmap.openGIS.UCFeatureLayer;

class EditOperation
{
	EditOperation(int type,UCFeatureLayer layer,Feature oldFeature,Feature newFeature)
	{
		this.type=type;
        this.layer=layer;
        this.oldFeature=oldFeature;
        this.newFeature=newFeature;
	}
    
    int type;
    Feature oldFeature;
    Feature newFeature;
    UCFeatureLayer layer;

    static final int AddFeature=0;//添加要素
    static final int DeleteFeature=1;//删除要素
    static final int UpdateFeature=2;//更新要素
    
    @Override
	public String toString() {
		return type+",layerName="+layer.getName()+",oldFID="+(oldFeature!=null?oldFeature.id():"null")+",newFID"+(newFeature!=null?newFeature.id():"null");
	}
}

public class UndoRedo
{
	
	private static UndoRedo instance;
	
	public static UndoRedo getInstance()
	{
		if (instance==null) instance=new UndoRedo();
		return instance;
	}
	
    UndoRedo()
    {
    	this.max=100;
        isBeginAdd=false;
        undoList=new LinkedList<Vector<EditOperation> >();
        redoList=new LinkedList<Vector<EditOperation> >();
        operations=new Vector<EditOperation>();
    }
    
    void setMax(int max)
    {
    	this.max=max;
    	undoList.clear();
    	redoList.clear();
    }
    
    boolean canUndo()//是否可以undo
    {
    	return !undoList.isEmpty();
    }
    
    boolean canRedo()//是否可以redo
    {
    	return !redoList.isEmpty();
    }
    
    int undo()//进行undo
    {
    	if (!canUndo()) return 0;
        Vector<EditOperation> ops=undoList.getLast();//.back();
        int count=ops.size();
        for (int i=0;i<count;++i)
        {
            EditOperation op=ops.get(i);
            switch (op.type)
            {
                case EditOperation.AddFeature:
                {
                	boolean ret=op.layer.deleteFeature(op.newFeature);
                    if (ret==false)
                        --count;
                    break;
                }
                case EditOperation.DeleteFeature:
                {
                	Hashtable<String,Object> values=new Hashtable<String,Object>();
                	for (Field f:op.oldFeature.schema())
                		if (op.oldFeature.get(f.name())!=null)
                			values.put(f.name(), op.oldFeature.get(f.name()));
                	Feature ft=op.layer.addFeature(values);
                	
                    if (ft==null)
                        --count;
                    else
                    {
                        //扫描undo列表，将之前AddFeature的操作中对应的要素id修改为之后的id        
                    	for (Iterator<Vector<EditOperation> > it=undoList.iterator();it.hasNext();)
                        {
                            Vector<EditOperation> ooo=it.next();
                            int cc=ooo.size();
                            for (int k=0;k<cc;++k)
                            {
                                EditOperation eo=ooo.get(k);
                                if (eo.type!=EditOperation.DeleteFeature && eo.layer==op.layer)
                                {
                                    //NSLog(@"%d,%d,%d,%d",eo.type,ft->getOid(),eo.oldFeature==0?0:eo.oldFeature->getOid(),eo.newFeature==0?0:eo.newFeature->getOid());
                                    if (eo.type!=EditOperation.AddFeature && eo.oldFeature!=null && eo.oldFeature.id()==op.oldFeature.id())
                                        eo.oldFeature=ft;
                                    if (eo.newFeature!=null && eo.newFeature.id()==op.oldFeature.id())
                                        eo.newFeature=ft;
                                }
                            }
                        }
                        op.oldFeature=ft;   
                    }
                    break;
                }
                case EditOperation.UpdateFeature:
                {
                	Hashtable<String,Object> values=new Hashtable<String,Object>();
                	for (Field f:op.oldFeature.schema())
                		if (op.oldFeature.get(f.name())!=null)
                			values.put(f.name(), op.oldFeature.get(f.name()));
                	Feature ft=op.layer.updateFeature(op.newFeature, values);
                    if (ft==null)
                        --count;
                    else
                        ft=null;
                    break;
                }
            }
        }
        if (max==redoList.size())
            removeFront(redoList);
        redoList.addLast(ops);//.push_back(ops);
        undoList.removeLast();//.pop_back();
        return count;
    }
    
    int getRedoCount()
    {
    	return redoList.size();
    }
    
    int redo()//进行redo
    {
    	if (!canRedo()) return -1;
        Vector<EditOperation> ops=redoList.getLast();//.back();
        int count=ops.size();
        for (int i=0;i<count;++i)
        {
            EditOperation op=ops.get(i);
            switch (op.type)
            {
                case EditOperation.AddFeature:
                {
                	Hashtable<String,Object> values=new Hashtable<String,Object>();
                	for (Field f:op.newFeature.schema())
                		if (op.newFeature.get(f.name())!=null)
                			values.put(f.name(), op.newFeature.get(f.name()));
                	Feature ft=op.layer.addFeature(values);
                	
                    if (ft==null)
                        --count;
                    else
                    {
                        //扫描redo列表，将之前DeleteFeature的操作中对应的要素id修改为之后的id
                    	for (Iterator<Vector<EditOperation> > it=redoList.iterator();it.hasNext();)
                        {
                            Vector<EditOperation> ooo=it.next();
                            int cc=ooo.size();
                            for (int k=0;k<cc;++k)
                            {
                                EditOperation eo=ooo.get(k);
                                if (eo.type!=EditOperation.AddFeature && eo.layer==op.layer)
                                {
                                    if (eo.oldFeature!=null && eo.oldFeature.id()==op.newFeature.id())
                                        eo.oldFeature=ft;
                                    if (eo.newFeature!=null && eo.newFeature.id()==op.newFeature.id())
                                        eo.newFeature=ft;
                                }
                            }
                        }
                    	op.newFeature=ft;
                    }
                    break;
                }
                case EditOperation.DeleteFeature:
                {
                	boolean ret=op.layer.deleteFeature(op.oldFeature);
                    if (ret==false)
                        --count;
                    break;
                }
                case EditOperation.UpdateFeature:
                {
                	Hashtable<String,Object> values=new Hashtable<String,Object>();
                	for (Field f:op.newFeature.schema())
                		if (op.newFeature.get(f.name())!=null)
                			values.put(f.name(), op.newFeature.get(f.name()));
                	Feature ft=op.layer.updateFeature(op.oldFeature, values);
                    if (ft==null)
                        --count;
                    else
                    	ft=null;
                    break;
                }
            }
        }
        if (max==undoList.size())
            removeFront(undoList);
        undoList.addLast(ops);//.push_back(ops);
        redoList.removeLast();//.pop_back();
        return count;
    }
    
    void clear()
    {
    	undoList.clear();
    	redoList.clear();
    }
    
    void addUndo(int type,UCFeatureLayer layer,Feature oldFeature,Feature newFeature)//添加一个操作
    {
    	if (layer==null || (oldFeature==null && newFeature==null)) return;
    	if (canRedo())
        {
            int count=redoList.size();
            for (int i=0;i<count;++i)
                removeFront(redoList);
        }
        if (isBeginAdd==false)
        {
            operations.clear();
            operations.add(new EditOperation(type,layer,oldFeature,newFeature));
            //undoList.push_back(operations);
            endAddUndo();
        }
        else
            operations.add(new EditOperation(type,layer,oldFeature,newFeature));
    }
    
    void beginAddUndo()//开始添加批量操作
    {
    	isBeginAdd=true;
        operations.clear();
    }
    
    void endAddUndo()//结束添加批量操作
    {
    	isBeginAdd=false;
        if (max==undoList.size())
            removeFront(undoList);
        undoList.add(new Vector<EditOperation>(operations));
    }

    LinkedList<Vector<EditOperation> > undoList;
    LinkedList<Vector<EditOperation> > redoList;
    int max;
    Vector<EditOperation> operations;
    boolean isBeginAdd;
    
    void removeFront(LinkedList<Vector<EditOperation> > list)
    {
    	if (list.isEmpty()) return;
        Vector<EditOperation> ops=list.getFirst();//.front();
        int count=ops.size();
        for (int i=0;i<count;++i)
        {
            EditOperation op=ops.get(i);
            op.oldFeature=null;
            op.newFeature=null;
        }
        list.removeFirst();//.pop_front();
    }
}
