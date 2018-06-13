package com.example.armangupta.photoblog;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    final static String TAG="HomeFragmentError";
    RecyclerView recyclerView;
    List<UserData> list;
    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    RecyclerPostAdapter recyclerPostAdapter;
    DocumentSnapshot lastVisible;
    boolean firstTimeAdded=true;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView=(RecyclerView)view.findViewById(R.id.recyclerView);
        list=new ArrayList<>();
        firebaseFirestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        recyclerPostAdapter=new RecyclerPostAdapter(list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recyclerPostAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                boolean reachedBottom=!recyclerView.canScrollVertically(1);
                if(reachedBottom){
                    loadMoreItems();
                }
            }
        });
        if(mAuth.getCurrentUser()!=null){
            Query firstquery=firebaseFirestore.collection("postedPhotos").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
            firstquery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                   if(e!=null){
                       Log.d(TAG, "onEvent: "+e.getMessage());
                   }
                   else
                   {
                       if(firstTimeAdded)
                       lastVisible=documentSnapshots.getDocuments().get(documentSnapshots.size()-1);

                       for(DocumentChange doc:documentSnapshots.getDocumentChanges()){
                           if(doc.getType()== DocumentChange.Type.ADDED){
                             String userId=doc.getDocument().getString("userId");
                             String imagethumb=doc.getDocument().getString("imagethumb");
                             String description=doc.getDocument().getString("description");
                             String imageUrl=doc.getDocument().getString("imageUrl");
                               Date timestamp=doc.getDocument().getDate("timestamp");
                               UserData userData=new UserData(userId,imagethumb,description,imageUrl,timestamp);
                               if(firstTimeAdded)
                             list.add(userData);
                               else
                                   list.add(0,userData);
                             recyclerPostAdapter.notifyDataSetChanged();
                           }
                       }
                       firstTimeAdded=false;
                   }
                }
            });
        }

        return view;

    }
    protected void loadMoreItems()
    {
        Query nextQuery=firebaseFirestore.collection("postedPhotos").orderBy("timestamp", Query.Direction.DESCENDING).startAfter(lastVisible).limit(3);
        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(e!=null)
                {
                    Log.d(TAG, "onEvent: "+e.getMessage());
                }
                else
                {
                    if(!documentSnapshots.isEmpty()) {
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String userId = doc.getDocument().getString("userId");
                                String imagethumb = doc.getDocument().getString("imagethumb");
                                String description = doc.getDocument().getString("description");
                                String imageUrl = doc.getDocument().getString("imageUrl");
                                Date timestamp = doc.getDocument().getDate("timestamp");
                                UserData userData = new UserData(userId, imagethumb, description, imageUrl, timestamp);
                                list.add(userData);
                                recyclerPostAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                    else{
                        Toast.makeText(getContext(),"You are all done for today",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

}
