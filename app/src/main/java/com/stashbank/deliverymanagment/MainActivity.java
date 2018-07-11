package com.stashbank.deliverymanagment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.*;
import android.widget.Button;
import android.support.v4.widget.*;
import android.support.design.widget.*;
import android.view.*;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v7.app.*;
import android.support.v4.view.*;
import android.widget.*;
import com.stashbank.deliverymanagment.models.*;
import com.stashbank.deliverymanagment.rest.*;
import retrofit2.*;
import android.util.*;

public class MainActivity extends AppCompatActivity implements OnNavigationItemSelectedListener,
	MainFragment.OnButtonClickListner,
	DeliveryFragment.DeliveryFragmentEventListener
{

	/**
	 * Id to identity CALL_PHONE permission request.
	 */
	private static final int REQUEST_CALL_PHONE = 0;
	private DrawerLayout drawer;
	private Toolbar toolbar;
	private NavigationView navigationView;
	private DeliveryItem selectedDeliveryItem;

	private DeliveryFragment deliveryFragment;
	private String phoneNumberToCall = null;

	private final int PAYMENT_REQ_CODE = 1;
	private FloatingActionButton fabNew, fabNewReceiving, fabNewDelivery;
	private Animation animOpen, animClose, animRotateClockwise, animRotateAnticlockwise;
	private int selectedMenuId = R.id.nav_home;
	private boolean isFabsOpen = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
		toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
			this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		if (savedInstanceState == null) {
			openMainFragment();
		}
		initFabs();
	}

	private  void initFabs() {
		android.content.Context ctx = getApplicationContext();
		animOpen = AnimationUtils.loadAnimation(ctx, R.anim.fab_open);
		animClose = AnimationUtils.loadAnimation(ctx, R.anim.fab_close);
		animRotateClockwise = AnimationUtils.loadAnimation(ctx, R.anim.rotate_clockwise);
		animRotateAnticlockwise = AnimationUtils.loadAnimation(ctx, R.anim.rotate_anticlockwise);

		fabNew = (FloatingActionButton) findViewById(R.id.fab_new);
		fabNewReceiving = (FloatingActionButton) findViewById(R.id.fab_new_receive);
		fabNewDelivery = (FloatingActionButton) findViewById(R.id.fab_new_delivery);
		fabNew.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (selectedMenuId == R.id.nav_home) {
					if (isFabsOpen)
						hideFabs();
					else
						showFabs();
				} else if (selectedMenuId == R.id.nav_delivery) {
					openCreateDeliveryCard();
				} else if (selectedMenuId == R.id.nav_shipping) {
					openCreateReceivingCard();
				}
			}
		});
		fabNewReceiving.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openCreateReceivingCard();
				hideFabs();
			}
		});
		fabNewDelivery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openCreateDeliveryCard();
				hideFabs();
			}
		});
	}

	private void showFabs() {
		fabNewReceiving.startAnimation(animOpen);
		fabNewDelivery.startAnimation(animOpen);
		fabNew.startAnimation(animRotateClockwise);
		fabNewReceiving.setClickable(true);
		fabNewDelivery.setClickable(true);
		isFabsOpen = true;
	}

	private void hideFabs() {
		fabNewReceiving.startAnimation(animClose);
		fabNewDelivery.startAnimation(animClose);
		fabNew.startAnimation(animRotateAnticlockwise);
		fabNewReceiving.setClickable(false);
		fabNewDelivery.setClickable(false);
		isFabsOpen = false;
	}

	private void openCreateDeliveryCard() {
		Toast.makeText(this, "Will be create new delivery", Toast.LENGTH_SHORT).show();
	}

	private void openCreateReceivingCard() {
		Toast.makeText(this, "Will be create new receiving", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.top_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem menuItem) {
		selectedMenuId = menuItem.getItemId();
		switch (selectedMenuId) {
			case R.id.nav_home:
				openMainFragment();
				break;
			case R.id.nav_delivery:
				openDeliveryFragment();
				break;
			case R.id.nav_shipping:
				openShippingFragment();
				break;
			case R.id.nav_share:
				Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
				break;
			case R.id.nav_send:
				Toast.makeText(this, "Send", Toast.LENGTH_SHORT).show();
				break;
		}
		drawer.closeDrawer(GravityCompat.START);

		return true;
	}

	private void openMainFragment() {
		selectedMenuId = R.id.nav_home;
		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.fragment_container, new MainFragment())
			.commit();
		navigationView.setCheckedItem(R.id.nav_home);

	}

	private void openDeliveryFragment() {
		selectedMenuId = R.id.nav_delivery;
		deliveryFragment = new DeliveryFragment();
		deliveryFragment.setEventListener(this);
		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.fragment_container, deliveryFragment)
			.commit();
		navigationView.setCheckedItem(R.id.nav_delivery);
	}

	private void openShippingFragment() {
		selectedMenuId = R.id.nav_shipping;
		ShippingFragment fragment = new ShippingFragment();
		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.fragment_container, fragment)
			.commit();
		navigationView.setCheckedItem(R.id.nav_shipping);
	}

	@Override
	public void onDeliveryButtonClick(View view)
	{
		openDeliveryFragment();
	}

	@Override
	public void onShippingButtonClick(View view)
	{
		openShippingFragment();
	}

	@Override
	public void onPaymentButtonClick(View view) {
		// TODO: Implement this method
		Toast.makeText(this, "Paymant button click", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void showProgress(final boolean show) {
		ProgressBar progressView = (ProgressBar) findViewById(R.id.progress);
		if (progressView != null) {
			FrameLayout frame = (FrameLayout) findViewById(R.id.fragment_container);
			progressView.setVisibility(show ? View.VISIBLE : View.GONE);
			// frame.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	public void overridePendingTransition(int enterAnim, int exitAnim) {
		// TODO: Implement this method
		super.overridePendingTransition(enterAnim, exitAnim);
	}

	@Override
	public void makePayment(DeliveryItem delivery) {
		selectedDeliveryItem = delivery;
		Intent intent = new Intent(this, PaymentActivity.class);
		intent.putExtra("number", delivery.getNumber());
		intent.putExtra("amount", delivery.getAmount());
		intent.putExtra("client", delivery.getClient());
		intent.putExtra("deliveryId", delivery.getId());
		startActivityForResult(intent, PAYMENT_REQ_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		showProgress(false);
		if (data == null) {return;}
		if (requestCode == PAYMENT_REQ_CODE) {
			onPaymentActivity(resultCode, data);
		}
	}

	void onPaymentActivity(int resultCode, Intent data) {
		boolean payed = data.getBooleanExtra("payed", false);
		if (resultCode == Activity.RESULT_OK && payed) {
			markAsPayed(selectedDeliveryItem);
			// Toast.makeText(this, "Payed", Toast.LENGTH_LONG).show();
		}
	}

	void markAsPayed(DeliveryItem item) {
		item.setPayed(true);
		setDeliveryItem(item);
	}

	public void makePhoneCall(String number) {
		int check = ContextCompat.checkSelfPermission( this, Manifest.permission.CALL_PHONE );
		if ( check != PackageManager.PERMISSION_GRANTED ) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				requestPermissions( new String[] {  Manifest.permission.CALL_PHONE }, REQUEST_CALL_PHONE);
			}
		} else {
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:" + number));
			startActivity(callIntent);
		}
	}

	@Override
	public void markAsDelivered(DeliveryItem item) {
		item.setDelivered(true);
		setDeliveryItem(item);
	}

	private void setDeliveryItem(DeliveryItem item) {
		DeliveryItemRepository repository = new DeliveryItemRepository();
		Call<DeliveryItem> call = repository.setItem(item.getId(), item);
		showProgress(true);
		call.enqueue(new Callback<DeliveryItem>() {
			@Override
			public void onResponse(Call<DeliveryItem> call, Response<DeliveryItem> response) {
				showProgress(false);
				if (response.isSuccessful()) {
					deliveryFragment.fetchData();
				} else {
					log("response code " + response.code());
				}
			}

			@Override
			public void onFailure(Call<DeliveryItem> call, Throwable t) {
				showProgress(false);
				log("ERROR " + t);
				// itemAdapter.onFetchDataFailure();
			}

		});
	}

	private void log(String message)
	{
		Log.d("REST", message);
	}

}
