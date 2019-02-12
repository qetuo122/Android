package chat.nra.com.chat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private ArrayList<ChatVO> list;
    private LayoutInflater inflater;
    private String id;

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder; // 데이터의 빠른 효율성을 위해 뷰 홀더 사용

        if(convertView == null){
            convertView = inflater.inflate(layout,parent,false);

            holder = new ViewHolder();
            holder.umsg = (TextView)convertView.findViewById(R.id.ucontent);
            holder.utime = (TextView)convertView.findViewById(R.id.utime);
            holder.uname = (TextView)convertView.findViewById(R.id.uid);
            holder.mmsg = (TextView)convertView.findViewById(R.id.mcontent);
            holder.mtime = (TextView)convertView.findViewById(R.id.mtime);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 채팅방에 입장한 아이디와 채팅리스트에 저장된 아이디가 같은경우 말풍선 오른쪽으로
        if(list.get(position).getUserName().equals(id)){
            //상대방의 말풍선
            holder.utime.setVisibility(View.GONE);
            holder.umsg.setVisibility(View.GONE);
            holder.uname.setVisibility(View.GONE);

            //나의 말풍선
            holder.mtime.setVisibility(View.VISIBLE);
            holder.mmsg.setVisibility(View.VISIBLE);

            holder.mtime.setText(list.get(position).getTime());
            holder.mmsg.setText(list.get(position).getMessage());
        } else {
            holder.utime.setVisibility(View.VISIBLE);
            holder.umsg.setVisibility(View.VISIBLE);
            holder.uname.setVisibility(View.VISIBLE);

            holder.mtime.setVisibility(View.GONE);
            holder.mmsg.setVisibility(View.GONE);

            holder.umsg.setText(list.get(position).getMessage());
            holder.utime.setText(list.get(position).getTime());
            holder.uname.setText(list.get(position).getUserName());
        }

        return convertView;
    }

    public ChatAdapter(Context context, int layout, ArrayList<ChatVO> list, String id) {
        this.context = context;
        this.layout = layout;
        this.list = list;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.id = id;
    }

    public class ViewHolder{
        TextView umsg;
        TextView utime;
        TextView uname;
        TextView mmsg;
        TextView mtime;
    }
}
