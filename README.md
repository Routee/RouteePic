# RouteePic
A module of PicSelector and Camera for Andorid devices

## *Step 1*.
#### Add the JitPack repository to your build file

```
allprojects {
	repositories {
    	...
    	maven { url 'https://jitpack.io' }
    }
}                       
```

## *Step 2*
#### Add the dependency
```
dependencies {
        implementation 'com.github.Routee:RouteePic:1.0.0'
}
```
[![](https://jitpack.io/v/Routee/RouteePic.svg)](https://jitpack.io/#Routee/RouteePic)

## *Step 3*
#### Usage

> 1 init the ImageEngin before start
```
ImageEngine.imageEngine = GlideImageLoader(application)
```

> 2 startActivity with the RouteePicEntity
```
var intent = Intent(this, RouteePicActivity::class.java)
var b = Bundle()
b.putSerializable(RouteePicEntity.ENTITY_NAME, mEntity)
intent.putExtras(b)
startActivityForResult(intent, REQUEST_CODE)
```

> 3 get the results with onActivityResult lifecycle
```
if (requestCode == REQUEST_CODE) {
    if (resultCode == RouteePicEntity.RESULT_CODE) {
        var b: Bundle = data?.extras ?: Bundle()
        var entity = b.getSerializable(RouteePicEntity.ENTITY_NAME) as RouteePicEntity
        mEntity.SELECTED_PICS.clear()
        mEntity.SELECTED_PICS.addAll(entity.SELECTED_PICS)
        mRvPic.adapter.notifyDataSetChanged()
    }
}
```

> you'd better download the sample for more usages
