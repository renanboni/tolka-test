package com.tolka.sample;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SimpleRvAlertDialogFragment extends DialogFragment
{
    private String m_strTitle;
    private List<String> m_dataList = new ArrayList<>();

    public static SimpleRvAlertDialogFragment newInstance( String title, List<String> dataList )
    {
        SimpleRvAlertDialogFragment dialog = new SimpleRvAlertDialogFragment();
        dialog.setTitle( title );
        dialog.setDataList( dataList );
        return dialog;
    }

    public void setDataList( List<String> dataList )
    {
        this.m_dataList = dataList;
        if ( m_dataList == null )
        {
            m_dataList = new ArrayList<>();
        }
    }

    public void setTitle( String title )
    {
        this.m_strTitle = title;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setLayout(width, height);
    }

    @Override
    public void onCreate( @Nullable Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialogStyle );
        setCancelable( true );
    }

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState )
    {
        View rootView = inflater.inflate( R.layout.dialog_fragment_simple_alert_rv, container, false );

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable( Color.TRANSPARENT));

        //hide system navigation bar
//        getDialog().getWindow().setFlags( WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        TextView textTitle = rootView.findViewById( R.id.text_title );
        textTitle.setText( m_strTitle );

        RecyclerView rvData = rootView.findViewById( R.id.rv_data );
        rvData.setLayoutManager( new LinearLayoutManager( getContext(), LinearLayoutManager.VERTICAL, false ) );
        rvData.setAdapter( new SimpleStringAdapter() );

        return rootView;
    }

    @Override
    public void onViewCreated(
            @NonNull View view, @Nullable  Bundle savedInstanceState )
    {
        super.onViewCreated( view, savedInstanceState );
    }

    class SimpleStringAdapter extends RecyclerView.Adapter<SimpleStringAdapter.ViewHolder>
    {
        class ViewHolder extends RecyclerView.ViewHolder
        {
            private TextView textName;
            public ViewHolder( @NonNull View itemView )
            {
                super( itemView );
                textName = (TextView) itemView;
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder( @NonNull ViewGroup parent, int viewType )
        {
            View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_in_rv_simple_string, parent,false );
            return new ViewHolder( view );
        }

        @Override
        public void onBindViewHolder( @NonNull ViewHolder holder, int position )
        {
            String name = m_dataList.get( position );
            holder.textName.setText( name );
        }

        @Override
        public int getItemCount()
        {
            return m_dataList.size();
        }
    }
}