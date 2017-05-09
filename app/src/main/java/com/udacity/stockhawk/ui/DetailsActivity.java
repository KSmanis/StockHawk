package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.BuildConfig;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity {

    private class HistoryLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (args == null || !args.containsKey(BUNDLE_KEY_SYMBOL)) {
                return null;
            }

            switch (id) {
            case HISTORY_LOADER_ID:
                return new CursorLoader(
                        DetailsActivity.this,
                        Contract.Quote.makeUriForStock(args.getString(BUNDLE_KEY_SYMBOL)),
                        new String[]{Contract.Quote.COLUMN_HISTORY},
                        null,
                        null,
                        null
                );
            default:
                throw new IllegalArgumentException("Unknown loader id: " + id);
            }
        }
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data == null || !data.moveToFirst()) {
                return;
            }

            plotChart(data.getString(data.getColumnIndex(Contract.Quote.COLUMN_HISTORY)));
        }
        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }

    public static final String EXTRA_SYMBOL = BuildConfig.APPLICATION_ID + ".SYMBOL";
    private static final int HISTORY_LOADER_ID = 0;
    private static final String BUNDLE_KEY_SYMBOL = "symbol";

    @BindView(R.id.line_chart)
    LineChart m_lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_SYMBOL)) {
            String symbol = intent.getStringExtra(EXTRA_SYMBOL);
            setTitle(getString(R.string.details_title, symbol));
            setupChart(symbol);

            Bundle bundle = new Bundle();
            bundle.putString(BUNDLE_KEY_SYMBOL, symbol);
            getSupportLoaderManager().initLoader(HISTORY_LOADER_ID, bundle, new HistoryLoaderCallbacks());
        }
    }

    private void setupChart(String symbol) {
        m_lineChart.setContentDescription(getString(R.string.a11y_chart, symbol));
        m_lineChart.setDescription(null);
        m_lineChart.getXAxis().setTextColor(ContextCompat.getColor(this, R.color.colorChartText));
        m_lineChart.getAxisLeft().setTextColor(ContextCompat.getColor(this, R.color.colorChartText));
        m_lineChart.getAxisRight().setTextColor(ContextCompat.getColor(this, R.color.colorChartText));
        m_lineChart.getLegend().setEnabled(false);
    }
    private void plotChart(String history) {
        if (TextUtils.isEmpty(history)) {
            return;
        }

        final List<String> dates = new ArrayList<>();
        List<Entry> values = new ArrayList<>();
        CSVReader reader = new CSVReader(new StringReader(history));
        try {
            float f = 0.f;
            for (String[] line; (line = reader.readNext()) != null;) {
                if (line.length != 2) {
                    continue;
                }

                dates.add(DateFormat.getDateFormat(this).format(new Date(Long.valueOf(line[0]))));
                values.add(0, new Entry(f--, Float.valueOf(line[1])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        LineDataSet lineDataSet = new LineDataSet(values, null);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setDrawValues(false);
        m_lineChart.setData(new LineData(lineDataSet));
        m_lineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return dates.get((int) -value);
            }
        });
        m_lineChart.invalidate();
    }
}
