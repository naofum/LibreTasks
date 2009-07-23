/*******************************************************************************
 * Copyright 2009 OmniDroid - http://code.google.com/p/omnidroid 
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 *******************************************************************************/
package edu.nyu.cs.omnidroid.ui.simple;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import edu.nyu.cs.omnidroid.R;
import edu.nyu.cs.omnidroid.ui.Constants;
import edu.nyu.cs.omnidroid.ui.simple.model.ModelEvent;
import edu.nyu.cs.omnidroid.ui.simple.model.ModelItem;
import edu.nyu.cs.omnidroid.ui.simple.model.ModelRuleAction;
import edu.nyu.cs.omnidroid.ui.simple.model.ModelRuleFilter;

/**
 * This activity is used to add multiple filters or actions to a new rule.
 */
public class ActivityChooseFiltersAndActions extends Activity {

  private ListView listview;
  private LinearLayout layoutButtonsFilter;
  private LinearLayout layoutButtonsTask;
  private AdapterRule adapterRule;
  private SharedPreferences state;

  public static final String KEY_STATE = "StateActivityChooseFilters";


  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_choose_filters_and_actions);

    // Link our listview with the AdapterRule instance.
    initializeListView();

    // Link up bottom button panel areas.
    initializeButtonPanel();
    
    // Restore UI state if possible.
    state = getSharedPreferences(ActivityChooseFiltersAndActions.KEY_STATE,
        Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
    listview.setItemChecked(state.getInt("selectedRuleItem", 0), true);
  }

  protected void onPause() {
    super.onPause();

    // Save UI state.
    SharedPreferences.Editor prefsEditor = state.edit();
    prefsEditor.putInt("selectedRuleItem", listview.getCheckedItemPosition());
    prefsEditor.commit();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
    case Constants.ACTIVITY_RESULT_ADD_FILTER:
      // Did the user construct a valid filter? If so add it to the rule.
      if (RuleBuilder.instance().getChosenRuleFilter() != null) {
        // Add the filter to the rule builder and the UI tree.
        adapterRule.addItemToParentPosition(listview.getCheckedItemPosition(), RuleBuilder
            .instance().getChosenRuleFilter());
      }
      RuleBuilder.instance().resetFilterPath();
      break;

    case Constants.ACTIVITY_RESULT_EDIT_FILTER:
      // Did the user modify a valid filter? If so replace it in the rule.
      if (RuleBuilder.instance().getChosenRuleFilter() != null) {
        // Add the filter to the rule builder and the UI tree.
        adapterRule.replaceItem(listview.getCheckedItemPosition(), RuleBuilder.instance()
            .getChosenRuleFilter());
      }
      RuleBuilder.instance().resetFilterPath();
      break;

    case Constants.ACTIVITY_RESULT_ADD_ACTION:
      // TODO: (markww) Just like above, try to build action from user input.
      break;

    case Constants.ACTIVITY_RESULT_EDIT_ACTION:
      // TODO: (markww) Just like above, try to edit action from user input.
      break;
    }
  }

  private void initializeListView() {
    listview = (ListView) findViewById(R.id.activity_choosefiltersandactions_listview);
    
    // After creating our adapter, have it render itself from the RuleBuilder data.
    adapterRule = new AdapterRule(this, listview);

    listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    listview.setAdapter(adapterRule);

    adapterRule.restoreFromRule();
  }
  
  private void initializeButtonPanel() {
    layoutButtonsFilter = (LinearLayout) findViewById(
      R.id.activity_choosefiltersandactions_llButtonsFilter);
    layoutButtonsTask = (LinearLayout) findViewById(
      R.id.activity_choosefiltersandactions_llButtonsAction);

    Button btnAddFilter = (Button) findViewById(
      R.id.activity_choosefiltersandactions_btnAddFilter);
    btnAddFilter.setOnClickListener(listenerBtnClickAddFilter);

    Button btnRemoveFilter = (Button) findViewById(
      R.id.activity_choosefiltersandactions_btnRemoveFilter);
    btnRemoveFilter.setOnClickListener(listenerBtnClickRemoveFilter);

    Button btnEditFilter = (Button) findViewById(
      R.id.activity_choosefiltersandactions_btnEditFilter);
    btnEditFilter.setOnClickListener(listenerBtnClickEditFilter);

    Button btnTasks = (Button) findViewById(
      R.id.activity_choosefiltersandactions_btnTasks);
    btnTasks.setOnClickListener(listenerBtnClickTasks);

    Button btnAddAction = (Button) findViewById(
      R.id.activity_choosefiltersandactions_btnAddAction);
    btnAddAction.setOnClickListener(listenerBtnClickAddAction);

    Button btnRemoveAction = (Button) findViewById(
      R.id.activity_choosefiltersandactions_btnRemoveAction);
    btnRemoveAction.setOnClickListener(listenerBtnClickRemoveAction);

    Button btnEditAction = (Button) findViewById(
      R.id.activity_choosefiltersandactions_btnEditAction);
    btnEditAction.setOnClickListener(listenerBtnClickEditAction);

    Button btnFilters = (Button) findViewById(
      R.id.activity_choosefiltersandactions_btnFilters);
    btnFilters.setOnClickListener(listenerBtnClickFilters);

    LinearLayout llBottomButtons = (LinearLayout) findViewById(
      R.id.activity_choosefiltersandactions_llBottomButtons);
    llBottomButtons.setBackgroundColor(getResources().getColor(R.color.layout_button_panel));
  }
  
  /**
   * Show filters button set.
   */
  private OnClickListener listenerBtnClickTasks = new OnClickListener() {
    public void onClick(View v) {
      layoutButtonsFilter.setVisibility(View.GONE);
      layoutButtonsTask.setVisibility(View.VISIBLE);
    }
  };

  /**
   * Show actions button set.
   */
  private OnClickListener listenerBtnClickFilters = new OnClickListener() {
    public void onClick(View v) {
      layoutButtonsTask.setVisibility(View.GONE);
      layoutButtonsFilter.setVisibility(View.VISIBLE);
    }
  };

  private OnClickListener listenerBtnClickAddFilter = new OnClickListener() {
    public void onClick(View v) {
      ModelItem selectedItem = adapterRule.getItem(listview.getCheckedItemPosition());
      if ((selectedItem instanceof ModelEvent) || (selectedItem instanceof ModelRuleFilter)) {
        // Now we present the user with a list of attributes they can
        // filter on for their chosen root event.
        showDlgAttributes();
      } else {
        UtilUI.showAlert(v.getContext(), "Sorry!",
            "Filters can only be added to the root event and other filters!");
        return;
      }
    }
  };

  private OnClickListener listenerBtnClickRemoveFilter = new OnClickListener() {
    public void onClick(View v) {
      int position = listview.getCheckedItemPosition();
      if (position == 0) {
        UtilUI.showAlert(v.getContext(), "Sorry!", "The root event cannot be removed!");
      } else if (position > 0 && position < adapterRule.getCount()) {
        adapterRule.removeItem(position);
      } else {
        UtilUI.showAlert(v.getContext(), "Sorry!",
            "Please select a filter from the list for removal!");
      }
    }
  };

  private OnClickListener listenerBtnClickEditFilter = new OnClickListener() {
    public void onClick(View v) {
      ModelItem item = adapterRule.getItem(listview.getCheckedItemPosition());
      if (item instanceof ModelRuleFilter) {
        editFilter(listview.getCheckedItemPosition(), (ModelRuleFilter) item);
      } else {
        UtilUI.showAlert(v.getContext(), "Sorry!", "Please select a filter from the list to edit!");
      }
    }
  };

  private OnClickListener listenerBtnClickAddAction = new OnClickListener() {
    public void onClick(View v) {
      // TODO: (markww) Re-enable adding actions.
      // For actions, we can simply ignore what item they have selected and
      // add the action directly to the root event. We may want to move
      // action additions to a separate activity later on anyway.
      UtilUI.showAlert(v.getContext(), "Sorry!",
        "Adding actions is not yet implemented!");
    }
  };

  private OnClickListener listenerBtnClickEditAction = new OnClickListener() {
    public void onClick(View v) {
      // TODO: (markww) re-enable editing of actions.
      UtilUI.showAlert(v.getContext(), "Sorry!",
        "Editing actions is not yet implemented!");
    }
  };

  private OnClickListener listenerBtnClickRemoveAction = new OnClickListener() {
    public void onClick(View v) {
      // TODO: (markww) Clean up action deletion.
      int position = listview.getCheckedItemPosition();
      if (position == 0) {
        UtilUI.showAlert(v.getContext(), "Sorry!", "The root event cannot be removed!");
      } else if (position > 0 && position < adapterRule.getCount()) {
        adapterRule.removeItem(position);
      } else {
        UtilUI.showAlert(v.getContext(), "Sorry!",
            "Please select an action from the list for removal!");
      }
    }
  };

  /**
   * Starts up the initial activity for adding a new filter.
   */
  private void showDlgAttributes() {
    // Reset the chosen filter data if left over from a previous run.
    RuleBuilder.instance().resetFilterPath();

    // Launch the activity chain for adding a filter. We check if the user completed a
    // filter in onActivityResult.
    Intent intent = new Intent();
    intent.setClass(getApplicationContext(), ActivityDlgAttributes.class);
    startActivityForResult(intent, Constants.ACTIVITY_RESULT_ADD_FILTER);
  }

  private void showDlgApplications() {
    // TODO: (markww) Add support for showing the applications dlg, works same way as filters.
  }

  /**
   * Starts up the one and only activity required to edit a filter.
   */
  private void editFilter(final int position, ModelRuleFilter filter) {
    // Set the filter data from the existing rule filter instance.
    RuleBuilder.instance().resetFilterPath();
    RuleBuilder.instance().setChosenModelFilter(filter.getModelFilter());
    RuleBuilder.instance().setChosenRuleFilterDataOld(filter.getData());

    Intent intent = new Intent();
    intent.setClass(getApplicationContext(), ActivityDlgFilterInput.class);
    startActivityForResult(intent, Constants.ACTIVITY_RESULT_EDIT_FILTER);
  }

  private void editAction(final int position, ModelRuleAction action) {
    // TODO: (markww) Add support for editing an action instance.
  }
}