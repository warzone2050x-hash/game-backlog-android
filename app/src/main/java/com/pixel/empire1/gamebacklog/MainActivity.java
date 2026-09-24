package com.pixel.empire1.gamebacklog;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private final List<GameEntry> games = new ArrayList<>();
    private final List<GameEntry> filtered = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
    private EditText search;
    private Spinner filter;
    private TextView stats;
    private ListView listView;

    private int dp(int v){ return Math.round(v * getResources().getDisplayMetrics().density); }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        games.addAll(GameStore.load(this));
        buildUi();
        refresh();
    }

    private TextView label(String s, int sp, boolean bold) {
        TextView v = new TextView(this);
        v.setText(s); v.setTextSize(sp); v.setTextColor(Color.WHITE);
        if (bold) v.setTypeface(Typeface.DEFAULT_BOLD);
        return v;
    }

    private Button btn(String s) {
        Button b = new Button(this);
        b.setText(s); b.setAllCaps(false);
        return b;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14),dp(20),dp(14),dp(10));
        root.setBackgroundColor(Color.rgb(10,13,18));

        TextView title = label("GAME BACKLOG",30,true);
        TextView sub = label("Retro & Classic Game Library",14,false);
        sub.setTextColor(Color.rgb(170,180,195));
        stats = label("",13,true);
        stats.setTextColor(Color.rgb(77,208,225));
        stats.setPadding(0,dp(12),0,dp(8));

        LinearLayout actions = new LinearLayout(this);
        Button add = btn("+ Add Game");
        Button random = btn("Random");
        LinearLayout.LayoutParams aw = new LinearLayout.LayoutParams(0,dp(48),1);
        aw.setMargins(dp(3),0,dp(3),0);
        actions.addView(add,aw); actions.addView(random,aw);

        search = new EditText(this);
        search.setHint("Search games...");
        search.setTextColor(Color.WHITE);
        search.setHintTextColor(Color.GRAY);
        search.setSingleLine(true);

        filter = new Spinner(this);
        String[] fs = {"All Games","Playing Now","Completed","Dropped","Wishlist"};
        filter.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,fs));

        LinearLayout controls = new LinearLayout(this);
        controls.addView(search,new LinearLayout.LayoutParams(0,dp(52),2));
        controls.addView(filter,new LinearLayout.LayoutParams(0,dp(52),1));

        listView = new ListView(this);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, new ArrayList<String>()) {
            @Override public View getView(int pos, View cv, ViewGroup parent) {
                View v = super.getView(pos,cv,parent);
                TextView t1 = v.findViewById(android.R.id.text1);
                TextView t2 = v.findViewById(android.R.id.text2);
                GameEntry g = filtered.get(pos);
                t1.setText(g.title);
                t1.setTextColor(Color.WHITE);
                t2.setText(g.platform + (g.year.isEmpty() ? "" : " • " + g.year) + "   [" + g.status + "]");
                t2.setTextColor(Color.rgb(170,180,195));
                v.setBackgroundColor(Color.rgb(18,24,35));
                return v;
            }
        };
        listView.setAdapter(listAdapter);

        root.addView(title); root.addView(sub); root.addView(stats); root.addView(actions); root.addView(controls);
        root.addView(listView,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1));
        setContentView(root);

        add.setOnClickListener(v -> editGame(null));
        random.setOnClickListener(v -> randomGame());
        listView.setOnItemClickListener((p,v,pos,id) -> editGame(filtered.get(pos)));
        listView.setOnItemLongClickListener((p,v,pos,id) -> { confirmDelete(filtered.get(pos)); return true; });
        search.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int b,int c){ refresh(); }
            public void afterTextChanged(Editable e){}
        });
        filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> p,View v,int pos,long id){ refresh(); }
            public void onNothingSelected(AdapterView<?> p){}
        });
    }

    private void refresh() {
        if (listAdapter == null) return;
        String q = search.getText().toString().trim().toLowerCase(Locale.ROOT);
        String f = filter.getSelectedItem()==null ? "All Games" : filter.getSelectedItem().toString();
        filtered.clear();
        for (GameEntry g : games) {
            String hay = (g.title+" "+g.platform+" "+g.year+" "+g.notes).toLowerCase(Locale.ROOT);
            if ((f.equals("All Games") || f.equals(g.status)) && (q.isEmpty() || hay.contains(q))) filtered.add(g);
        }
        Collections.sort(filtered,(a,b)->a.title.compareToIgnoreCase(b.title));
        listAdapter.clear();
        for (GameEntry g : filtered) listAdapter.add(g.title);
        listAdapter.notifyDataSetChanged();
        stats.setText("TOTAL "+games.size()+"   •   PLAYING "+count("Playing Now")+"   •   COMPLETED "+count("Completed")+"   •   WISHLIST "+count("Wishlist"));
    }

    private int count(String s){ int n=0; for(GameEntry g:games) if(s.equals(g.status)) n++; return n; }

    private EditText field(String hint,String value){
        EditText e=new EditText(this); e.setHint(hint); e.setText(value==null?"":value); e.setTextColor(Color.WHITE); e.setHintTextColor(Color.GRAY); return e;
    }

    private void editGame(GameEntry existing){
        LinearLayout form=new LinearLayout(this); form.setOrientation(LinearLayout.VERTICAL); form.setPadding(dp(18),0,dp(18),0);
        EditText title=field("Game title *", existing==null?"":existing.title);
        EditText platform=field("Platform", existing==null?"":existing.platform);
        EditText year=field("Year", existing==null?"":existing.year);
        EditText notes=field("Notes", existing==null?"":existing.notes);
        Spinner status=new Spinner(this);
        status.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,GameEntry.STATUSES));
        if(existing!=null) for(int i=0;i<GameEntry.STATUSES.length;i++) if(GameEntry.STATUSES[i].equals(existing.status)) status.setSelection(i);
        else status.setSelection(3);
        form.addView(title); form.addView(platform); form.addView(year); form.addView(status); form.addView(notes);

        AlertDialog d=new AlertDialog.Builder(this).setTitle(existing==null?"Add Game":"Edit Game").setView(form)
                .setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        d.setOnShowListener(x -> d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String t=title.getText().toString().trim();
            if(t.isEmpty()){ title.setError("Required"); return; }
            if(existing==null) games.add(new GameEntry(System.currentTimeMillis(),t,platform.getText().toString().trim(),year.getText().toString().trim(),status.getSelectedItem().toString(),notes.getText().toString().trim()));
            else { existing.title=t; existing.platform=platform.getText().toString().trim(); existing.year=year.getText().toString().trim(); existing.status=status.getSelectedItem().toString(); existing.notes=notes.getText().toString().trim(); }
            GameStore.save(this,games); refresh(); d.dismiss();
        }));
        d.show();
    }

    private void confirmDelete(GameEntry g){
        new AlertDialog.Builder(this).setTitle("Delete "+g.title+"?").setNegativeButton("Cancel",null)
                .setPositiveButton("Delete",(d,w)->{ games.remove(g); GameStore.save(this,games); refresh(); }).show();
    }

    private void randomGame(){
        List<GameEntry> src=filtered.isEmpty()?games:filtered;
        if(src.isEmpty()){ Toast.makeText(this,"Add a game first.",Toast.LENGTH_SHORT).show(); return; }
        GameEntry g=src.get(new Random().nextInt(src.size()));
        new AlertDialog.Builder(this).setTitle("🎲 "+g.title)
                .setMessage(g.platform+(g.year.isEmpty()?"":" • "+g.year)+"\n\nStatus: "+g.status)
                .setPositiveButton("Play This",(d,w)->{ g.status="Playing Now"; GameStore.save(this,games); refresh(); })
                .setNegativeButton("Close",null).show();
    }
}
