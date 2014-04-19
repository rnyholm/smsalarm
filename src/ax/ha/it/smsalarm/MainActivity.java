package ax.ha.it.smsalarm;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.KeyEvent;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity {

	private Fragment content;
	private ListFragment fragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set title to this activity (maybe not needed)
		setTitle(R.string.APP_NAME);

		// Set behind and content view
		setBehindContentView(R.layout.menu_frame);
		setContentView(R.layout.content_frame);

		// Set correct fragment to menu and correct content to this object
		setFragmentToMenu(savedInstanceState);
		setContentFragment(savedInstanceState);

		// Set correct content to content frame
		getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame_fl, content).commit();

		// Configurate the sliding menu
		configureSlidingMenu();

		// Configure action bar
		configureActionBar();
	}

	private void configureActionBar() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher);
		setSlidingActionBarEnabled(true);
	}

	private void configureSlidingMenu() {
		// configure the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
	}

	private void setFragmentToMenu(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			fragment = new SlidingMenuFragment();// new RandomList();
			ft.replace(R.id.menuFrame_fl, fragment);
			ft.commit();
		} else {
			fragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.menuFrame_fl);
		}
	}

	private void setContentFragment(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			content = getSupportFragmentManager().getFragment(savedInstanceState, "content");
		}
		if (content == null) {
			content = new SmsSettingsView();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, "content", content);
	}

	@Override
	public void onBackPressed() {
		if (getSlidingMenu().isMenuShowing()) {
			toggle();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			toggle();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				toggle();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void switchContent(Fragment fragment) {
		content = fragment;
		getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame_fl, fragment).commit();
		getSlidingMenu().showContent();
	}
}