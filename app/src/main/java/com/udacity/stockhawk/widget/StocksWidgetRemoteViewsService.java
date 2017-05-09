package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.DetailsActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class StocksWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor m_data;
            private DecimalFormat m_dollarFormat;
            private DecimalFormat m_dollarFormatWithPlus;
            private DecimalFormat m_percentageFormat;

            @Override
            public void onCreate() {
                m_dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                m_dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                m_dollarFormatWithPlus.setPositivePrefix(getString(R.string.positive_prefix_absolute));
                m_percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                m_percentageFormat.setMaximumFractionDigits(2);
                m_percentageFormat.setMinimumFractionDigits(2);
                m_percentageFormat.setPositivePrefix(getString(R.string.positive_prefix_percentage));
            }
            @Override
            public void onDataSetChanged() {
                if (m_data != null) {
                    m_data.close();
                }

                final long callingIdentity = Binder.clearCallingIdentity();
                m_data = getContentResolver().query(
                        Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL
                );
                Binder.restoreCallingIdentity(callingIdentity);
            }
            @Override
            public void onDestroy() {
                if (m_data != null) {
                    m_data.close();
                    m_data = null;
                }
            }
            @Override
            public int getCount() {
                return (m_data != null ? m_data.getCount() : 0);
            }
            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION || m_data == null ||
                        !m_data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote);

                String symbol = m_data.getString(Contract.Quote.POSITION_SYMBOL);
                views.setTextViewText(R.id.symbol, symbol);
                views.setContentDescription(R.id.symbol, getString(R.string.a11y_quote_symbol, symbol));

                String price = m_dollarFormat.format(m_data.getFloat(Contract.Quote.POSITION_PRICE));
                views.setTextViewText(R.id.price, price);
                views.setContentDescription(R.id.price, getString(R.string.a11y_quote_price, price));

                float absoluteChange = m_data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                float percentageChange = m_data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
                String change = (PrefUtils.getDisplayMode(StocksWidgetRemoteViewsService.this)
                        .equals(getString(R.string.pref_display_mode_absolute_key)) ?
                        m_dollarFormatWithPlus.format(absoluteChange) :
                        m_percentageFormat.format(percentageChange / 100));
                views.setInt(R.id.change, "setBackgroundResource", absoluteChange > 0 ?
                        R.drawable.percent_change_pill_green :
                        R.drawable.percent_change_pill_red);
                views.setTextViewText(R.id.change, change);
                views.setContentDescription(R.id.change, getString(R.string.a11y_quote_change, change));

                views.setOnClickFillInIntent(R.id.list_item_quote, new Intent()
                        .putExtra(DetailsActivity.EXTRA_SYMBOL, symbol));

                return views;
            }
            @Override
            public RemoteViews getLoadingView() {
                return null;
            }
            @Override
            public int getViewTypeCount() {
                return 1;
            }
            @Override
            public long getItemId(int position) {
                return m_data.moveToPosition(position) ? m_data.getLong(Contract.Quote.POSITION_ID) : -1L;
            }
            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
