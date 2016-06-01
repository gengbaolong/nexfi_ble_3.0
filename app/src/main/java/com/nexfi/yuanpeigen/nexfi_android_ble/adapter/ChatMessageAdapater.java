package com.nexfi.yuanpeigen.nexfi_android_ble.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.activity.BigImageActivity;
import com.nexfi.yuanpeigen.nexfi_android_ble.activity.ModifyInformationActivity;
import com.nexfi.yuanpeigen.nexfi_android_ble.activity.UserInformationActivity;
import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.FileMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.MessageBodyType;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.SingleChatMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.TextMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.FileTransferUtils;

import java.util.List;


/**
 * Created by Mark on 2016/4/25.
 */
public class ChatMessageAdapater extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<SingleChatMessage> coll;
    private Context mContext;
    private String userSelfId;

    public final static int SEND_LEFT = 20;
    public final static int SEND_RIGHT = 21;
    public final static int IMAGE_LEFT = 22;
    public final static int IMAGE_RIGHT = 23;


    public ChatMessageAdapater(Context context, List<SingleChatMessage> coll, String userSelfId) {
        this.coll = coll;
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.userSelfId = userSelfId;
    }


    private static final String TAG = ChatMessageAdapater.class.getSimpleName();

    @Override
    public int getViewTypeCount() {
        return 30;
    }

    @Override
    public int getItemViewType(int position) {
        SingleChatMessage entity = coll.get(position);
        switch (entity.messageBodyType) {
            case MessageBodyType.eMessageBodyType_Text:
                if (entity.userMessage.userId.equals(userSelfId)) {
                    return SEND_RIGHT;
                } else {
                    return SEND_LEFT;
                }
            case MessageBodyType.eMessageBodyType_Image:
                if (entity.userMessage.userId.equals(userSelfId)) {
                    return IMAGE_RIGHT;
                } else {
                    return IMAGE_LEFT;
                }
        }
        return -1;
    }

    @Override
    public int getCount() {
        return coll.size();
    }

    @Override
    public Object getItem(int position) {
        return coll.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        SingleChatMessage entity = coll.get(position);
        int messageBodyType = entity.messageBodyType;
        TextMessage textMsg = null;
        FileMessage fileMsg = null;
        switch (messageBodyType) {
            case MessageBodyType.eMessageBodyType_Text://文本消息
                textMsg = entity.textMessage;
                break;
            case MessageBodyType.eMessageBodyType_Image://图片
                fileMsg = entity.fileMessage;
                break;
        }


        ViewHolder_chatSend viewHolder_chatSend = null;
        ViewHolder_chatReceive viewHolder_chatReceive = null;
        ViewHolder_sendFile viewHolder_sendFile = null;
        ViewHolder_ReceiveFile viewHolder_receiveFile = null;
        ViewHolder_sendImage viewHolder_sendImage = null;
        ViewHolder_ReceiveImage viewHolder_receiveImage = null;


        if (convertView == null) {
            viewHolder_chatSend = new ViewHolder_chatSend();
            viewHolder_sendImage = new ViewHolder_sendImage();
            switch (messageBodyType) {
                case MessageBodyType.eMessageBodyType_Text:
                    if (entity.userMessage.userId.equals(userSelfId)) {//自己是发送(右)，别人是接收(左)
//                        convertView = mInflater.inflate(R.layout.item_chatting_msg_send, null);
                        convertView=View.inflate(mContext,R.layout.item_chatting_msg_send, null);
                    } else {
                        convertView = mInflater.inflate(R.layout.item_chatting_msg_receive, null);
                    }

                    viewHolder_chatSend.tv_chatText_send = (TextView) convertView.findViewById(R.id.tv_chatText_send);
                    viewHolder_chatSend.tv_sendTime_send = (TextView) convertView.findViewById(R.id.tv_sendtime_send);
                    viewHolder_chatSend.iv_userhead_send_chat = (ImageView) convertView.findViewById(R.id.iv_userhead_send);
                    convertView.setTag(viewHolder_chatSend);
                    break;

                case MessageBodyType.eMessageBodyType_Image:
                    if (entity.userMessage.userId.equals(userSelfId)) {//自己是发送(右)，别人是接收(左)
                        convertView = mInflater.inflate(R.layout.item_send_imge, null);
                    } else {
                        convertView = mInflater.inflate(R.layout.item_recevied_imge, null);
                    }
                    viewHolder_sendImage.chatcontent_send = (RelativeLayout) convertView.findViewById(R.id.chatcontent_send);
                    viewHolder_sendImage.iv_icon_send = (ImageView) convertView.findViewById(R.id.iv_icon_send);
                    viewHolder_sendImage.iv_userhead_send_image = (ImageView) convertView.findViewById(R.id.iv_userhead_send_image);
                    viewHolder_sendImage.tv_sendTime_send_image = (TextView) convertView.findViewById(R.id.tv_sendTime_send_image);
                    viewHolder_sendImage.pb_send = (ProgressBar) convertView.findViewById(R.id.pb_send);
                    convertView.setTag(viewHolder_sendImage);
                    break;
            }
        } else {

            switch (messageBodyType) {
                case MessageBodyType.eMessageBodyType_Text:
                    viewHolder_chatSend = (ViewHolder_chatSend) convertView.getTag();
                    break;
                case MessageBodyType.eMessageBodyType_Image:
                    viewHolder_sendImage = (ViewHolder_sendImage) convertView.getTag();
                    break;
            }

        }

        switch (messageBodyType) {
            case MessageBodyType.eMessageBodyType_Text:

                viewHolder_chatSend.iv_userhead_send_chat.setImageResource(BleApplication.iconMap.get(entity.userMessage.userAvatar));

                if (entity.userMessage.userId.equals(userSelfId)) {
                    viewHolder_chatSend.iv_userhead_send_chat.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, ModifyInformationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        }
                    });
                } else {
                    viewHolder_chatSend.iv_userhead_send_chat.setOnClickListener(new AvatarClick(position));
                }

                viewHolder_chatSend.tv_sendTime_send.setText(entity.timeStamp);
                viewHolder_chatSend.tv_chatText_send.setText(textMsg.fileData);

                break;
            case MessageBodyType.eMessageBodyType_Image:
                try {
                    final byte[] bys_send = Base64.decode(fileMsg.fileData, Base64.DEFAULT);

                    if (bys_send != null) {
                        Bitmap bitmap = FileTransferUtils.getPicFromBytes(bys_send);
                        viewHolder_sendImage.iv_icon_send.setImageBitmap(bitmap);
                        viewHolder_sendImage.iv_icon_send.setScaleType(ImageView.ScaleType.FIT_XY);

                        viewHolder_sendImage.iv_userhead_send_image.setImageResource(BleApplication.iconMap.get(entity.userMessage.userAvatar));

                        if (entity.userMessage.userId.equals(userSelfId)) {
                            viewHolder_sendImage.iv_userhead_send_image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(mContext, ModifyInformationActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mContext.startActivity(intent);
                                }
                            });
                        } else {
                            viewHolder_sendImage.iv_userhead_send_image.setOnClickListener(new AvatarClick(position));
                        }

                        viewHolder_sendImage.tv_sendTime_send_image.setText(entity.timeStamp);
                        if (fileMsg.isPb == 0) {
                            viewHolder_sendImage.pb_send.setVisibility(View.INVISIBLE);
                        } else {
                            viewHolder_sendImage.pb_send.setVisibility(View.VISIBLE);
                        }
                        viewHolder_sendImage.chatcontent_send.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(mContext, BigImageActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("bitmap", bys_send);
                                mContext.startActivity(intent);
                                ((Activity) mContext).overridePendingTransition(R.anim.img_scale_in, R.anim.img_scale_out);
                            }
                        });
                    }
                } catch (OutOfMemoryError error) {
                    //
                }
                break;
        }
        return convertView;
    }


    //单击事件实现
    class AvatarClick implements View.OnClickListener {
        public int position;

        public AvatarClick(int p) {
            position = p;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(mContext, UserInformationActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("data_obj", coll.get(position).userMessage);
            intent.putExtras(bundle);
            mContext.startActivity(intent);
        }
    }

    static class ViewHolder_chatSend {
        public TextView tv_chatText_send, tv_sendTime_send;
        public ImageView iv_userhead_send_chat;
    }

    static class ViewHolder_chatReceive {
        public TextView tv_chatText_receive, tv_sendTime_receive;
        public ImageView iv_userhead_receive_chat;
    }

    static class ViewHolder_sendFile {
        public TextView tv_sendTime_send_folder, tv_size_send, tv_file_name_send;
        public ImageView iv_userhead_send_folder, iv_icon_send;
        public RelativeLayout chatcontent_send;
        public ProgressBar pb_send;
    }

    static class ViewHolder_ReceiveFile {
        public TextView tv_sendTime_receive_folder, tv_size_receive, tv_file_name_receive;
        public ImageView iv_userhead_receive_folder, iv_icon_receive;
        public ProgressBar pb_receive;
        public RelativeLayout chatcontent_receive;
    }

    static class ViewHolder_ReceiveImage {
        public TextView tv_sendTime_receive_image;
        public ImageView iv_userhead_receive_image, iv_icon_receive;
        public ProgressBar pb_receive;
        public RelativeLayout chatcontent_receive;
    }

    static class ViewHolder_sendImage {
        public TextView tv_sendTime_send_image;
        public ImageView iv_userhead_send_image, iv_icon_send;
        public RelativeLayout chatcontent_send;
        public ProgressBar pb_send;
    }
}