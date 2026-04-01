package com.example.zenchat.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Magnifier
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getColor
import com.example.zenchat.R
import com.example.zenchat.data.model.NavItem
import com.example.zenchat.databinding.BottomNavViewBinding

@SuppressLint("ClickableViewAccessibility")
@RequiresApi(Build.VERSION_CODES.Q)
class BottomNavView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
	
	private val binding = BottomNavViewBinding.inflate(LayoutInflater.from(context), this, true)
	private var magnifier: Magnifier? = null
	private lateinit var navItems: List<NavItem>
	
	private val zoomIn by lazy { AnimationUtils.loadAnimation(context, R.anim.zoom_in) }
	private val zoomOut by lazy { AnimationUtils.loadAnimation(context, R.anim.zoom_out) }
	private var selectedNavItem: NavItem? = null
	
	init {
		orientation = HORIZONTAL
		val activity = context.getActivity()
		val decorView = activity?.window?.decorView
		
		if (decorView != null) {
			magnifier = Magnifier.Builder(decorView)
				.setSize(250, 210)
				.setCornerRadius(105f)
				.setInitialZoom(1.5f)
				.build()
		}
		
		navItems = listOf(
			NavItem(
				binding.navChat,
				binding.iconChat,
				binding.textChat
			),
			NavItem(
				binding.navSettings,
				binding.iconSettings,
				binding.textSettings
			)
		)
		navItems.forEach { item ->
			item.layout.isClickable = true
			item.layout.isFocusable = true
			
			item.layout.setOnTouchListener { view, event ->
				view.showMovingTouchPointer(binding.root, event.rawX, event.rawY, event.action)
				true
			}
			
			item.layout.setOnClickListener {
				selectItem(item, zoomIn, zoomOut)
				
				if (item.layout == binding.navSettings) {
					(context as? BottomNavActivity)?.showSettings()
				} else if (item.layout == binding.navChat) {
					(context as? BottomNavActivity)?.showHome()
				}
			}
		}
		if (navItems.isNotEmpty()) {
			selectItem(navItems[0], zoomIn, zoomOut)
		}
	}
	
	private fun selectItem(selected: NavItem, zoomIn: Animation, zoomOut: Animation) {
		if (selectedNavItem == selected) return
		val previouslySelectedItem = selectedNavItem
		if (previouslySelectedItem != null) {
			previouslySelectedItem.icon.startAnimation(zoomOut)
			previouslySelectedItem.icon.setColorFilter(getColor(context, R.color.grey_300))
			previouslySelectedItem.label.setTextColor(getColor(context, R.color.nav_unselected))
		}
		selected.icon.startAnimation(zoomIn)
		selected.icon.setColorFilter(getColor(context,R.color.blue))
		selected.label.setTextColor(getColor(context, R.color.nav_selected))
		selected.label.typeface = Typeface.DEFAULT_BOLD
		selectedNavItem = selected
	}
	
	@SuppressLint("ClickableViewAccessibility", "NewApi")
	fun View.showMovingTouchPointer(
		clampingView: View,
		rawX: Float,
		rawY: Float,
		action: Int,
	) {
		val pointerWidth = 210f
		val pointerHeight = 210f
		val pointerRadiusX = pointerWidth / 2f
		val pointerRadiusY = pointerHeight / 2f
		
		val screenPos = IntArray(2)
		clampingView.getLocationOnScreen(screenPos)
		val viewRect = android.graphics.Rect(
			screenPos[0],
			screenPos[1],
			screenPos[0] + clampingView.width,
			screenPos[1] + clampingView.height
		)
		val effectiveClampingRadius = maxOf(pointerRadiusX, pointerRadiusY)
		
		val minX = viewRect.left + effectiveClampingRadius
		val maxX = viewRect.right - effectiveClampingRadius
		val minY = viewRect.top + effectiveClampingRadius
		val maxY = viewRect.bottom - effectiveClampingRadius
		
		val clampedRawX = rawX.coerceIn(minX, maxX)
		val clampedRawY = rawY.coerceIn(minY, maxY)
		when (action) {
			MotionEvent.ACTION_DOWN -> {
				val childScreenPos = IntArray(2)
				this.getLocationOnScreen(childScreenPos)
				val childRect = android.graphics.Rect(
					childScreenPos[0],
					childScreenPos[1],
					childScreenPos[0] + this.width,
					childScreenPos[1] + this.height
				)
				if (childRect.contains(rawX.toInt(), rawY.toInt())) {
					magnifier?.show(clampedRawX, clampedRawY, clampedRawX, clampedRawY)
				}
			}
			
			MotionEvent.ACTION_MOVE -> {
				val itemAtClampedPoint = findNavItemAt(clampedRawX, clampedRawY)
				if (itemAtClampedPoint != null) {
					magnifier?.show(clampedRawX, clampedRawY, clampedRawX, clampedRawY)
					
				} else {
					magnifier?.dismiss()
				}
			}
			
			MotionEvent.ACTION_UP -> {
				magnifier?.dismiss()
				val targetItem = findNavItemAt(clampedRawX, clampedRawY)
				targetItem?.layout?.performClick()
			}
			
			MotionEvent.ACTION_CANCEL -> {
				magnifier?.dismiss()
			}
		}
	}
	
	fun Context.getActivity(): android.app.Activity? {
		return when (this) {
			is android.app.Activity -> this
			is android.content.ContextWrapper -> baseContext.getActivity()
			else -> null
		}
	}
	
	private fun findNavItemAt(rawX: Float, rawY: Float): NavItem? {
		val touchX = rawX.toInt()
		val touchY = rawY.toInt()
		val location = IntArray(2)
		return navItems.find { item ->
			item.layout.getLocationOnScreen(location)
			val rect = android.graphics.Rect(
				location[0],
				location[1],
				location[0] + item.layout.width,
				location[1] + item.layout.height
			)
			rect.contains(touchX, touchY)
		}
	}
	
	companion object{
	
	}
}