package com.cniao5.cniao5shop.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.cniao5.cniao5shop.MyApplication;
import com.cniao5.cniao5shop.R;
import com.cniao5.cniao5shop.adapter.AddressAdapter;
import com.cniao5.cniao5shop.adapter.decoration.DividerItemDecortion;
import com.cniao5.cniao5shop.bean.Address;
import com.cniao5.cniao5shop.http.OkHttpHelper;
import com.cniao5.cniao5shop.http.SpotsCallBack;
import com.cniao5.cniao5shop.msg.BaseResMsg;
import com.cniao5.cniao5shop.widget.Constants;
import com.cniao5.cniao5shop.widget.CustomDialog;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.okhttp.Response;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地址列表
 */
public class AddressListActivity extends BaseActivity {

    @ViewInject(R.id.recycler_view)
    private RecyclerView mRecyclerview;

    private AddressAdapter mAdapter;

    private OkHttpHelper mHttpHelper = OkHttpHelper.getInstance();

    private CustomDialog mDialog;

    @Override
    public int getLayoutId() {
        return R.layout.activity_address_list;
    }

    @Override
    public void init() {
        initAddress();
    }

    @Override
    public void setToolbar() {
        getToolbar().setTitle("我的地址");
        getToolbar().setleftButtonIcon(R.drawable.icon_back_32px);
        getToolbar().setRightImgButtonIcon(R.drawable.icon_add_w);
        getToolbar().setRightButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                toAddActivity();
            }
        });
    }

    /**
     * 显示删除提示对话框
     *
     * @param address
     */
    private void showDialog(final Address address) {
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setMessage("您确定删除该地址吗？");
        builder.setTitle("友情提示");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteAddress(address);
                initAddress();

                if (mDialog.isShowing())
                    mDialog.dismiss();
            }
        });

        builder.setNegativeButton("取消",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mDialog.isShowing())
                            mDialog.dismiss();
                    }
                });

        mDialog = builder.create();
        mDialog.show();

    }

    /**
     * 删除地址
     *
     * @param address
     */
    private void deleteAddress(Address address) {
        Map<String, String> params = new HashMap<>(1);
        params.put("id", address.getId() + "");

        mHttpHelper.doPost(Constants.API.ADDR_DEL, params, new SpotsCallBack<BaseResMsg>(AddressListActivity.this) {

            @Override
            public void onSuccess(Response response, BaseResMsg resMsg) {
                if (resMsg.getStatus() == resMsg.STATUS_SUCCESS) {
                    setResult(RESULT_OK);
                    System.out.println("----------------" + resMsg.getStatus());
                    if (mDialog.isShowing())
                        mDialog.dismiss();
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {

            }
        });
    }

    /**
     * 跳转到添加地址页面
     * 点击右上角添加按钮，传入TAG_SAVE,更改添加地址页面toolbar显示
     */
    private void toAddActivity() {

        Intent intent = new Intent(this, AddressAddActivity.class);
        intent.putExtra("tag", Constants.TAG_SAVE);
        startActivityForResult(intent, Constants.ADDRESS_ADD);
    }

    /**
     * 跳转AddressAddActivity页面结果处理
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        initAddress();
    }

    /**
     * 初始化地址页面
     */
    private void initAddress() {
        String userId = MyApplication.getInstance().getUser().getId() + "";

        if (!TextUtils.isEmpty(userId)) {
            Map<String, String> params = new HashMap<>(1);

            params.put("user_id", userId);

            mHttpHelper.doGet(Constants.API.ADDR_LIST, params, new SpotsCallBack<List<Address>>(this) {

                @Override
                public void onSuccess(Response response, List<Address> addresses) {
                    showAddress(addresses);
                }

                @Override
                public void onError(Response response, int code, Exception e) {

                }
            });
        }
    }

    /**
     * 显示地址列表
     *
     * @param addresses
     */
    private void showAddress(final List<Address> addresses) {

        Collections.sort(addresses);
        if (mAdapter == null) {
            mAdapter = new AddressAdapter(this, addresses, new AddressAdapter.AddressLisneter() {
                @Override
                public void setDefault(Address address) {

                    setResult(RESULT_OK);
                    //更改地址
                    updateAddress(address);
                }

                @Override
                public void onClickEdit(Address address) {
                    editAddress(address);
                }

                @Override
                public void onClickDelete(Address address) {
                    showDialog(address);
                    mDialog.show();
                }
            });
            mRecyclerview.setAdapter(mAdapter);
            mRecyclerview.setLayoutManager(new LinearLayoutManager(AddressListActivity.this));
            mRecyclerview.addItemDecoration(new DividerItemDecortion(this, DividerItemDecortion.VERTICAL_LIST));
        } else {
            mAdapter.refreshData(addresses);
            mRecyclerview.setAdapter(mAdapter);
        }

    }

    /**
     * 编辑地址
     * 传入TAG_COMPLETE更改AddressAddActivitytoolbar显示
     *
     * @param address
     */
    private void editAddress(Address address) {
        Intent intent = new Intent(this, AddressAddActivity.class);
        intent.putExtra("tag", Constants.TAG_COMPLETE);
        intent.putExtra("addressBean", address);

        startActivityForResult(intent, Constants.ADDRESS_EDIT);
    }

    /**
     * 更新地址
     *
     * @param address
     */
    public void updateAddress(Address address) {

        Map<String, String> params = new HashMap<>(1);
        params.put("id", address.getId() + "");
        params.put("consignee", address.getConsignee());
        params.put("phone", address.getPhone());
        params.put("addr", address.getAddr());
        params.put("zip_code", address.getZipCode());
        params.put("is_default", address.getIsDefault() + "");


        mHttpHelper.doPost(Constants.API.ADDR_UPDATE, params, new SpotsCallBack<BaseResMsg>(this) {

            @Override
            public void onSuccess(Response response, BaseResMsg resMsg) {
                if (resMsg.getStatus() == resMsg.STATUS_SUCCESS) {

                    //从服务端更新地址
                    initAddress();
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {

            }
        });

    }

}
