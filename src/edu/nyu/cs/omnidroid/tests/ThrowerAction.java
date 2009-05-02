package edu.nyu.cs.omnidroid.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.nyu.cs.omnidroid.R;
import edu.nyu.cs.omnidroid.util.AGParser;
import edu.nyu.cs.omnidroid.util.StringMap;
import edu.nyu.cs.omnidroid.util.UGParser;

/**
 * Presents a list of possible actions that the selected <code>ActionThrower</code> could throw as
 * it's action to perform for that OmniHandler.
 * 
 * @author acase
 */
public class ThrowerAction extends ListActivity {

  // Intent Data provided by the user so far
  private String eventApp;
  private String eventName;
  private String filterType;
  private String filterData;
  private String throwerApp;

  // Standard Menu options (Android menus require int)
  private static final int MENU_HELP = 0;

  /*
   * (non-Javadoc)
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Get passed to us to use
    Intent i = getIntent();
    Bundle extras = i.getExtras();
    if (extras != null) {
      eventApp = extras.getString(AGParser.KEY_APPLICATION);
      eventName = extras.getString(UGParser.KEY_EventName);
      // TODO(acase): Allow more than one filter
      filterType = extras.getString(UGParser.KEY_FilterType);
      filterData = extras.getString(UGParser.KEY_FilterData);
      throwerApp = extras.getString(UGParser.KEY_ActionApp);
    }

    // Getting the Actions from AppConfig
    AGParser ag = new AGParser(getApplicationContext());
    ArrayList<HashMap<String, String>> eventList = ag.readActions(throwerApp);
    Iterator<HashMap<String, String>> i1 = eventList.iterator();
    ArrayList<StringMap> stringvalues = new ArrayList<StringMap>();
    while (i1.hasNext()) {
      HashMap<String, String> HM1 = i1.next();
      HM1.toString();
      String splits[] = HM1.toString().split(",");
      for (int cnt = 0; cnt < splits.length; cnt++) {
        // Reformat the data so we can use it
        String pairs[] = splits[cnt].split("=");
        String key = pairs[0].replaceFirst("\\{", "");
        String entry = pairs[1].replaceFirst("\\}", "");
        stringvalues.add(new StringMap(key, entry));
      }
    }

    // Build our list of Actions
    ArrayAdapter<StringMap> arrayAdapter = new ArrayAdapter<StringMap>(this,
        android.R.layout.simple_list_item_1, stringvalues);
    setListAdapter(arrayAdapter);
    getListView().setTextFilterEnabled(true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int,
   * long)
   */
  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    StringMap sm = (StringMap) l.getAdapter().getItem(position);
    Intent i = new Intent();
    i.setClass(this.getApplicationContext(), ThrowerData.class);
    i.putExtra(AGParser.KEY_APPLICATION, eventApp);
    i.putExtra(UGParser.KEY_EventName, eventName);
    if ((filterType != null) && (filterData != null)) {
      i.putExtra(UGParser.KEY_FilterType, filterType);
      i.putExtra(UGParser.KEY_FilterData, filterData);
    }
    i.putExtra(UGParser.KEY_ActionApp, throwerApp);
    i.putExtra(UGParser.KEY_ActionName, sm.getKey());

    // If there isn't any data that we can apply then go to the next page
    AGParser ag = new AGParser(this.getApplicationContext());
    ArrayList<String> data = ag.readURIFields(throwerApp, sm.getKey());
    if (data.size() == 0) {
      i.setClass(this.getApplicationContext(), Throwers.class);
      setResult(Constants.RESULT_SUCCESS, i);
      finish();
    } else {
      startActivityForResult(i, Constants.RESULT_ADD_THROWER);      
    }

    
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
   */
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
    case Constants.RESULT_ADD_THROWER:
      switch (resultCode) {
      case Constants.RESULT_SUCCESS:
        setResult(resultCode, data);
        finish();
        break;
      }
      break;
    }
  }

  /**
   * Creates the options menu items
   * 
   * @param menu
   *          - the options menu to create
   */
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, MENU_HELP, 0, R.string.help).setIcon(android.R.drawable.ic_menu_help);
    return true;
  }

  /**
   * Handles menu item selections
   */
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case MENU_HELP:
      help();
      return true;
    }
    return false;
  }

  /**
   * Call our help dialog
   */
  private void help() {
    Builder help = new AlertDialog.Builder(this);
    // TODO(acase): Move to some kind of resource
    String help_msg = "Select the action that you want the selected catcher application to respond with.";
    help.setTitle(R.string.help);
    help.setIcon(android.R.drawable.ic_menu_help);
    help.setMessage(Html.fromHtml(help_msg));
    help.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
      }
    });
    help.show();
  }

}