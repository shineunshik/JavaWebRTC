package com.codewithkael.javawebrtcyoutube.remote;

import androidx.annotation.NonNull;

import com.codewithkael.javawebrtcyoutube.utils.DataModel;
import com.codewithkael.javawebrtcyoutube.utils.ErrorCallBack;
import com.codewithkael.javawebrtcyoutube.utils.NewEventCallBack;
import com.codewithkael.javawebrtcyoutube.utils.SuccessCallBack;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.Objects;

public class FirebaseClient {

    private final Gson gson = new Gson();
    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private String currentUsername;
    private static final String LATEST_EVENT_FIELD_NAME = "latest_event";

    public void login(String username, SuccessCallBack callBack){
//        dbRef.child("은식이").child(username).setValue("").addOnCompleteListener(task -> { //나를 등록한다 (환자) 진료신청 //이부분만 의사 앱에서 빼야함 (진료신청할떄 화상통화대기실에 진료신청한 의사리스트에 넣는다)
//            currentUsername = username;
//            callBack.onSuccess();
//        });
        dbRef.child("화상통화대기실").child(username).setValue("").addOnCompleteListener(task -> { //나를 등록한다 (환자) 진료신청(화상통화대기실에 의사별로 리스트를 만든다)
            currentUsername = username;
            callBack.onSuccess();
        });

    }

    public void sendMessageToOtherUser(DataModel dataModel, ErrorCallBack errorCallBack){//전화를 걸 사람을 찾는다 (의사) 의사 앱에 들어가서 대기 목록을 찾는다
      //  dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
        dbRef.child("화상통화대기실").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(dataModel.getTarget()).exists()){
                    //send the signal to other user
                    dbRef.child("화상통화대기실").child(dataModel.getTarget()).child(LATEST_EVENT_FIELD_NAME) //전화를 걸 사람의 정보에 업데이트한다
                            .setValue(gson.toJson(dataModel));


                }else {
                    errorCallBack.onError();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorCallBack.onError();
            }
        });
    }

    public void observeIncomingLatestEvent(NewEventCallBack callBack){ //통화가 걸려오면
        dbRef.child("화상통화대기실").child(currentUsername).child(LATEST_EVENT_FIELD_NAME).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try{
                            String data= Objects.requireNonNull(snapshot.getValue()).toString();
                            DataModel dataModel = gson.fromJson(data,DataModel.class);
                            callBack.onNewEventReceived(dataModel);  //통화가 걸려오면 걸려온 전화의 정보를 보낸다 mainrepository 호출
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );


    }
}
