package com.app.teachingassistant.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.app.teachingassistant.Chat_Activity;
import com.app.teachingassistant.DAO.AccountDAO;
import com.app.teachingassistant.DAO.ClassDAO;
import com.app.teachingassistant.R;
import com.app.teachingassistant.TeacherClass;
import com.app.teachingassistant.config.BackgroundDrawable;
import com.app.teachingassistant.config.Student_Attendance_List_Recycle_Adapter;
import com.app.teachingassistant.model.Attendance_Infor;
import com.app.teachingassistant.model.StudentAttendInfor;
import com.app.teachingassistant.model.StudentBannedInfor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Student_Home_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Student_Home_Fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = Student_Home_Fragment.class.getSimpleName();;
    private static final String ARG_PARAM2 = "param2";
    int a = 5;

    // TODO: Rename and change types of parameters
    View view;
    private String mParam1;
    private String mParam2;
    ArrayList<Attendance_Infor> attendance_list = new ArrayList<Attendance_Infor>();
    LinearLayout classEventLayout;
    RelativeLayout classInforLayout;
    Spinner classitemOption;
    Button openMessBtn;
    RecyclerView attendance_list_recycler_view;
    FirebaseUser user;
    ImageView background;
    DatabaseReference classRef,userRef;
    TextView className,teacherName,studentName,totalStudyTxt,totalLateTxt,totalAbsentTxt,stateTxt;
    Student_Attendance_List_Recycle_Adapter student_attendance_list_recycle_adapter;

    public Student_Home_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Studdent_Home_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Student_Home_Fragment newInstance(String param1, String param2) {
        Student_Home_Fragment fragment = new Student_Home_Fragment();
        Bundle args = new Bundle();
        args.putString(TAG, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(TAG);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        user = FirebaseAuth.getInstance().getCurrentUser();
        classRef = FirebaseDatabase.getInstance().getReference("Class");
        userRef = FirebaseDatabase.getInstance().getReference("Users");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.student_class_home_fragment, container, false);
        //Khai báo các thành phần trong layout fragment
        classEventLayout = view.findViewById(R.id.event_available_layout);
        classInforLayout = view.findViewById(R.id.student_main_class_item_relative);
        classitemOption = view.findViewById(R.id.class_item_more_option);
        attendance_list_recycler_view = view.findViewById(R.id.attendance_list);
        className = view.findViewById(R.id.student_home_classname);
        teacherName = view.findViewById(R.id.student_home_teacher_name);
        background = view.findViewById(R.id.class_background_image);


        studentName = view.findViewById(R.id.textView);
        totalStudyTxt = view.findViewById(R.id.total_study);
        totalLateTxt = view.findViewById(R.id.total_late);
        totalAbsentTxt = view.findViewById(R.id.total_absent);
        stateTxt = view.findViewById(R.id.status);






        //Thay đổi các thông tin của layout thông tin lớp học, ẩn đi phần event do xài lại layout
        float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,150, this.getResources().getDisplayMetrics());
        float margin_pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,10, this.getResources().getDisplayMetrics());
        classEventLayout.setVisibility(View.GONE);
        classitemOption.setVisibility(View.GONE);
        RelativeLayout.LayoutParams params = new  RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,(int)pixels);
        params.setMargins((int)margin_pixels,(int)margin_pixels,(int)margin_pixels,(int)margin_pixels);
        classInforLayout.setLayoutParams(params);


        //Load danh sách điểm danh
        student_attendance_list_recycle_adapter = new Student_Attendance_List_Recycle_Adapter(getActivity(),attendance_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        attendance_list_recycler_view.setItemAnimator(new DefaultItemAnimator());
        attendance_list_recycler_view.setLayoutManager(layoutManager);
        attendance_list_recycler_view.setAdapter(student_attendance_list_recycle_adapter);


        loadAll();
        return view;
    }
    public void loadAttendancelist(){
        String keyID = ClassDAO.getInstance().getCurrentClass().getKeyID();
        DatabaseReference attendRef = FirebaseDatabase.getInstance().getReference("Attendances").child(keyID);
        attendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot item:snapshot.getChildren()){
                    Attendance_Infor temp = item.getValue(Attendance_Infor.class);
                    attendance_list.add(0,temp);
                    student_attendance_list_recycle_adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadStudentState(){
        studentName.setText(AccountDAO.getInstance().getCurrentUser().getName());
        totalStudyTxt.setText(String.valueOf(ClassDAO.getInstance().getCurrentClass().getClassPeriod()));
        String UUID = FirebaseAuth.getInstance().getUid();
        final int[] lated = {0};
        final int[] absent = {0};
        FirebaseDatabase.getInstance().getReference("Attendances")
                .child(ClassDAO.getInstance().getCurrentClass().getKeyID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            for (DataSnapshot item: snapshot.getChildren()) {
                                DataSnapshot temp = item.child("studentStateList");
                                if (temp.getValue() != null) {
                                    for (DataSnapshot data: temp.getChildren()) {
                                        if (data != null) {
                                            StudentAttendInfor std = data.getValue(StudentAttendInfor.class);
                                            if (std.getUUID().equals(UUID)) {
                                                if (std.getState() == 0) {
                                                    lated[0] = lated[0] +1;

                                                }
                                                else if (std.getState() < 0) {
                                                    absent[0] = absent[0] +1;
                                                }
                                            }
                                        }
                                    }
                                }
                                totalLateTxt.setText(lated[0]+" buổi");
                                totalAbsentTxt.setText(absent[0]+" buổi");
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        FirebaseDatabase.getInstance().getReference("Class").child(ClassDAO.getInstance().getCurrentClass()
                .getKeyID()).child("studentBannedList").child(UUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!=null){
                    StudentBannedInfor data = snapshot.getValue(StudentBannedInfor.class);
                    if(data.getState() == -1){
                        stateTxt.setText("Cấm thi");
                        stateTxt.setTextColor(getResources().getColor(R.color.chi_gong));

                    }
                    else {
                        stateTxt.setText("Bình thường");
                        stateTxt.setTextColor(getResources().getColor(R.color.mint_leaf));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void finish(){
        finish();
    }
    private void loadAll(){
        className.setText(ClassDAO.getInstance().getCurrentClass().getClassName());
        teacherName.setText(ClassDAO.getInstance().getCurrentClass().getTeacherName());
        background.setImageResource(BackgroundDrawable.getInstance().getBackGround(ClassDAO.getInstance().getCurrentClass().getBackgroundtheme()));
        loadAttendancelist();
        loadStudentState();

    }
}