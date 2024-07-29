package com.example.expensemanagerdep;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.expensemanagerdep.Model.Data;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.util.ArrayList;

import java.util.Map;
import java.util.TreeMap;

import com.github.mikephil.charting.charts.PieChart;


public class StatsFragment extends Fragment {

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;
    private DatabaseReference mExpenseDatabase;
    private String[] type = {"Income", "Expense"};
    private int[] values = {0, 0};
    private Map<Date, Integer> DateWiseIncome = new TreeMap<>();
    private Map<Date, Integer> DateWiseExpense = new TreeMap<>();

    private PieChart pieChart;
    private LineChart incomeLineChart, expenseLineChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_stats, container, false);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mIncomeDatabase = FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid);
        mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseData").child(uid);
        mIncomeDatabase.keepSynced(true);
        mExpenseDatabase.keepSynced(true);

        // Initialize the charts
        pieChart = myView.findViewById(R.id.piechart);
        incomeLineChart = myView.findViewById(R.id.linechart);
        expenseLineChart = myView.findViewById(R.id.lineChart1);

        loadIncomeData();
        loadExpenseData();

        return myView;
    }

    private void loadIncomeData() {
        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                values[0] = 0;
                DateWiseIncome.clear();
                for (DataSnapshot mysnap : snapshot.getChildren()) {
                    Data data = mysnap.getValue(Data.class);
                    values[0] += data.getAmount();
                    DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
                    Date date = null;
                    try {
                        date = format.parse(data.getDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    DateWiseIncome.put(date, DateWiseIncome.getOrDefault(date, 0) + data.getAmount());
                }
                updatePieChart();
                updateLineChart(incomeLineChart, DateWiseIncome, "Income", 0xFF669900);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadExpenseData() {
        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                values[1] = 0;
                DateWiseExpense.clear();
                for (DataSnapshot mysnap : snapshot.getChildren()) {
                    Data data = mysnap.getValue(Data.class);
                    values[1] += data.getAmount();
                    DateFormat format = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
                    Date date = null;
                    try {
                        date = format.parse(data.getDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    DateWiseExpense.put(date, DateWiseExpense.getOrDefault(date, 0) + data.getAmount());
                }
                updatePieChart();
                updateLineChart(expenseLineChart, DateWiseExpense, "Expense", 0xFFCC0000);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updatePieChart() {
        ArrayList<PieEntry> data = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            data.add(new PieEntry(values[i], type[i]));
        }

        int[] colorClassArray = new int[]{0xFF669900, 0xFFCC0000};
        PieDataSet pieDataSet = new PieDataSet(data, "");
        pieDataSet.setColors(colorClassArray);
        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(25);
        pieChart.setData(pieData);
        pieChart.animateXY(2000, 2000);
        pieChart.setDrawHoleEnabled(false);
        Legend l = pieChart.getLegend();
        l.setTextSize(18);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setTextColor(Color.CYAN);
        l.setEnabled(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }

    private void updateLineChart(LineChart lineChart, Map<Date, Integer> dataMap, String label, int color) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        String[] xAxisValues = new String[dataMap.size()];
        ArrayList<Entry> entries = new ArrayList<>();
        int i = 0;
        for (Map.Entry<Date, Integer> entry : dataMap.entrySet()) {
            Format formatter = new SimpleDateFormat("MMM d, yyyy");
            String s = formatter.format(entry.getKey());
            xAxisValues[i] = s;
            entries.add(new Entry(i, entry.getValue()));
            i++;
        }

        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(color);
        set.setValueTextColor(Color.CYAN);
        set.setValueTextSize(15);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setLineWidth(4f);
        set.setCircleRadius(3f);
        dataSets.add(set);

        YAxis rightYAxis = lineChart.getAxisRight();
        rightYAxis.setTextColor(Color.BLUE);
        YAxis leftYAxis = lineChart.getAxisLeft();
        leftYAxis.setEnabled(true);
        leftYAxis.setTextColor(Color.BLUE);
        XAxis topXAxis = lineChart.getXAxis();
        topXAxis.setEnabled(true);
        topXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextColor(Color.BLUE);
        lineChart.getXAxis().setLabelCount(dataMap.size());
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisValues));

        LineData data = new LineData(dataSets);
        lineChart.setData(data);

        lineChart.animateX(3000);
        lineChart.getLegend().setEnabled(false);
        lineChart.invalidate();
        lineChart.getDescription().setEnabled(false);
    }
}
