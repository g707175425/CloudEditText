# CloudEditText 
#### (EditText内容分不同块显示,支持校验,删除块,添加块,得到块代表的字符串集合)

### 预览效果:
![](https://github.com/g707175425/CloudEditText/blob/master/CloudEditText.gif)

### 代码中实现:

#### 1.继承CloudEditText覆写checkInputSpan实现校验(不需校验可以省略本步)
```java	
	public class ContactCloudEditTextImpl extends CloudEditText {

	    public ContactCloudEditTextImpl(Context context) {
	        super(context);
	    }
	
	    public ContactCloudEditTextImpl(Context context, AttributeSet attrs) {
	        super(context, attrs);
	    }
	
	    public ContactCloudEditTextImpl(Context context, AttributeSet attrs, int defStyle) {
	        super(context, attrs, defStyle);
	    }
	
	    @Override
	    public boolean checkInputSpan(String showText, String returnText) {
	        if(returnText.contains("@")){
	            boolean result = CheckUtils.checkEmail(returnText);
	            if(!result){
	                ToastUtils.showErrorToast(getContext(),"请输入一个邮箱",0);
	            }
	            return result;
	        }else{
	            if(returnText.contains("+")){
	                ToastUtils.showErrorToast(getContext(),"手机号前请不要加区号",0);
	                return false;
	            }else{
	                boolean result = CheckUtils.checkMobile(returnText);
	                if(!result){
	                    ToastUtils.showErrorToast(getContext(),"请输入一个手机号",0);
	                }
	                return result;
	            }
	        }
	    }
	}
```

#### 2.在Xml中引入
```xml
	<cn.schope.lightning.view.ContactCloudEditTextImpl
                android:inputType="textEmailAddress|textMultiLine"
                android:id="@+id/et_user_contact"
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:layout_toLeftOf="@+id/v_choose_contact"
                android:background="#00000000"
                android:hint="手机号/邮箱"
                android:padding="10dp"
                android:textColorHint="@color/lowGrayText" />
```
#### 3.在Activity或Fragment中添加一个块或获得所有块的字符串集合(默认输入法中回车将字符串转化为块)
```java	
	et_user_contact.addSpan(contacts.get(0).name,contacts.get(0).contact);//添加块	
	et_user_contact.getAllReturnStringList();//获得所有块的字符串集合
```



by QQ:707175425
