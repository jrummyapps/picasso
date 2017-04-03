/*
 * Copyright (C) 2016 Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.picasso;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jrummyapps.picasso.Picasso;

import java.util.List;

public class SampleAppIconsActivity extends PicassoSampleActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .add(R.id.sample_content, ListFragment.newInstance())
          .commit();
    }
  }

  public static class ListFragment extends Fragment {

    public static ListFragment newInstance() {
      return new ListFragment();
    }

    private ListView listView;
    private AppAdapter adapter;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
      final SampleAppIconsActivity activity = (SampleAppIconsActivity) getActivity();

      listView = (ListView) LayoutInflater.from(activity)
          .inflate(R.layout.sample_list_detail_list, container, false);
      listView.setOnScrollListener(new SampleScrollListener(activity));
      listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
          Intent intent = getContext().getPackageManager()
              .getLaunchIntentForPackage(adapter.getItem(position).packageName);
          if (intent != null) {
            try {
              startActivity(intent);
            } catch (ActivityNotFoundException ignored) {
            }
          }
        }
      });
      return listView;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
      new AsyncTask<Void, Void, List<PackageInfo>>() {

        @Override protected List<PackageInfo> doInBackground(Void... params) {
          return getContext().getPackageManager().getInstalledPackages(0);
        }

        @Override protected void onPostExecute(List<PackageInfo> packageInfos) {
          adapter = new AppAdapter(packageInfos);
          listView.setAdapter(adapter);
        }
      }.execute();
    }
  }

  static class AppAdapter extends BaseAdapter {

    private final List<PackageInfo> packageInfos;

    public AppAdapter(List<PackageInfo> packageInfos) {
      this.packageInfos = packageInfos;
    }

    @Override public int getCount() {
      return packageInfos.size();
    }

    @Override public PackageInfo getItem(int position) {
      return packageInfos.get(position);
    }

    @Override public long getItemId(int position) {
      return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
      Context context = parent.getContext();
      ViewHolder holder;
      if (convertView == null) {
        convertView = LayoutInflater.from(context)
            .inflate(R.layout.sample_list_detail_item, parent, false);
        holder = new ViewHolder();
        holder.image = (ImageView) convertView.findViewById(R.id.photo);
        holder.text = (TextView) convertView.findViewById(R.id.url);
        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      PackageInfo packageInfo = getItem(position);

      holder.text.setText(packageInfo.packageName);

      // Trigger the download of the URL asynchronously into the image view.
      Picasso.with(context)
          .load(Picasso.SCHEME_PACKAGE + ":" + packageInfo.packageName)
          .placeholder(android.R.drawable.sym_def_app_icon)
          .error(android.R.drawable.sym_def_app_icon)
          .tag(context)
          .into(holder.image);

      return convertView;
    }

    static class ViewHolder {

      ImageView image;
      TextView text;
    }

  }

}
