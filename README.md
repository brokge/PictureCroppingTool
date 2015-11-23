# picturetool
单个图片选择和裁剪，一般用于头像项目

##使用方式

* 当前的Activity继承 
 ```java
 public class MainActivity extends PictureBaseActivity{
 //..... todo
 }
```
* 点击调用 
 ```java
showChoiceDialog();
```
* 重写：
``` java
  @Override
    public void onCompressed(Uri uri) {
        super.onCompressed(uri);
        //tudo

    }

    @Override
    public void onPictureCropped(Uri uri) {
        super.onPictureCropped(uri);
        //uri methods : getpath() etc.
        mImageView.setImageBitmap(BitmapUtil.decodeUriAsBitmap(this, uri));
    }
```


