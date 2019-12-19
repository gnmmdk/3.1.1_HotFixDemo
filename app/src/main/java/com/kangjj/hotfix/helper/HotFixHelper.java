package com.kangjj.hotfix.helper;

import android.content.Context;
import android.util.Log;

import org.joor.Reflect;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: jj.kang
 * @Email: 345498912@qq.com
 * @ProjectName: 3.1.1_HotFixDemo
 * @Package: com.kangjj.hotfix.helper
 * @CreateDate: 2019/12/19 21:28
 */
public class HotFixHelper {

    private static final String TAG = "HotFixHelper";

    private static final String PATCH_DIR = "patch_dir";

    public static boolean loadPath(Context context,String assetName){
        File pathDir = new File(context.getFilesDir(),PATCH_DIR);
        try {
            FileUtil.makeAndEnsureDirExisted(pathDir);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"create patch dir failed");
            return false;
        }
        File patchFile = new File(pathDir,assetName);
        return FileUtil.copyAsset2Dst(context,assetName,patchFile);
    }

    public static boolean deletePathFile(Context context,String assetName){
        File patchFile = new File(new File(context.getFilesDir(), PATCH_DIR), assetName);
        if(patchFile == null || !patchFile.exists()){
            return false;
        }
        return patchFile.delete();
    }

    public static void tryInjectDex(Context context, String assetName) {
        File patchFile = new File(new File(context.getFilesDir(),PATCH_DIR),assetName);
        if(patchFile!=null && patchFile.exists()){
            List<File> files = new ArrayList<>();
            files.add(patchFile);
            try {
                injectDex(context,context.getClassLoader(),files);
                Log.d(TAG, "inject dex success!");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "inject dex failed:" + e.toString());
            }
        }
    }

    private static void injectDex(Context context, ClassLoader loader, List<File> extraDexFiles) throws IOException{
        Object pathList = Reflect.on(loader).field("pathList").get();//BaseDexClassLoader 的 DexPathList  pathList
        ArrayList<IOException> suppressedExceptions = new ArrayList<>();
        Object[] patchDexElements = makePatchElements(pathList,extraDexFiles,FileUtil.getDexOptDir(context),suppressedExceptions);
        if(suppressedExceptions.size()>0){
            for (IOException e : suppressedExceptions) {
                Log.w(TAG, "Exception in makePathElement", e);
                throw e;
            }
        }
        // 将补丁Dex注入到系统ClassLoader的pathList对象的dexElements的最前面
        expandElementsArray(pathList,patchDexElements);
    }

    private static void expandElementsArray(Object pathList, Object[] extraElements) {
        Object[] originalDexElements = Reflect.on(pathList).field("dexElements").get();
        Object[] combined = (Object[]) Array.newInstance(originalDexElements.getClass().getComponentType(),
                originalDexElements.length+extraElements.length);
        // 注意此处拷贝顺序，将补丁Dex放到了最前面
        System.arraycopy(extraElements,0,combined,0,extraElements.length);
        System.arraycopy(originalDexElements,0,combined,extraElements.length,originalDexElements.length);
        Reflect.on(pathList).set("dexElements", combined);
    }

//    返回Element[]
    private static Object[] makePatchElements(Object dexPathList, List<File> files, File optimizedDirectory, ArrayList<IOException> suppressedExceptions) {
        return Reflect.on(dexPathList).call("makePathElements",files,optimizedDirectory,suppressedExceptions).get();//
    }
}
