package Utils;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.platform.EnumUtils;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.platform.win32.FlagEnum;
import com.sun.jna.platform.win32.Guid.CLSID;
import com.sun.jna.platform.win32.Guid.IID;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.WinDef.BOOL;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

// Thanks to https://github.com/matthiasblaesing/ for the help provided
// Copy pasted from https://github.com/matthiasblaesing/JNA-Demos/blob/master/IDesktopWallpaper/src/main/java/eu/doppel_helix/dev/blaesing/IDesktopWallpaper/DesktopWallpaper.java

public class DesktopWallpaper extends Unknown {
	// Determined with OleView by looking through the "All Objects" list
	public static final CLSID CLSID = new CLSID("{C2CF3110-460E-4FC1-B9D0-8A1C0C9CC4BD}");
	// Determined from the header file "ShObjIdl_core.h"
	public static final IID IID = new IID("{B92B56A9-8B55-4E14-9A89-0199BBB6F93B}");

	// VTable IDs are determined from the ShObjIdl_core.h header and need to
	// considert, that IDesktopWallpaper is derived from IUnknown (that is the
	// base class of all COM objects). IUnknown contains 3 methods and so the
	// first vtable id of the derivates is 3.
	private static final int VTABLE_ID_SET_WALLPAPER = 3;
	private static final int VTABLE_ID_GET_WALLPAPER = 4;
	private static final int VTABLE_ID_GET_MONITOR_DEVICE_PATH_AT = 5;
	private static final int VTABLE_ID_GET_MONITOR_DEVICE_PATH_COUNT = 6;
	private static final int VTABLE_ID_GET_MONITOR_RECT = 7;
	private static final int VTABLE_ID_SET_POSITION = 8;
	private static final int VTABLE_ID_GET_POSITION = 9;
	private static final int VTABLE_ID_SET_SLIDESHOW = 10;
	private static final int VTABLE_ID_GET_SLIDESHOW = 11;
	private static final int VTABLE_ID_SET_SLIDESHOW_OPTIONS = 12;
	private static final int VTABLE_ID_GET_SLIDESHOW_OPTIONS = 13;
	private static final int VTABLE_ID_ADVANCE_SLIDESHOW = 14;
	private static final int VTABLE_ID_GET_STATUS = 15;
	private static final int VTABLE_ID_ENABLE = 16;

	// Enums are just ints with special meaning - the FlagEnum combined with
	// EnumUtils and/or EnumConverter can be used to convert to named values
	public enum DESKTOP_WALLPAPER_POSITION implements FlagEnum {
		DWPOS_CENTER(0),
		DWPOS_TILE(1),
		DWPOS_STRETCH(2),
		DWPOS_FIT(3),
		DWPOS_FILL(4),
		DWPOS_SPAN(5);

		private final int flag;

		private DESKTOP_WALLPAPER_POSITION(int flag) {
			this.flag = flag;
		}

		@Override
		public int getFlag() {
			return flag;
		}
	}

	public DesktopWallpaper() {
	}

	public DesktopWallpaper(Pointer pvInstance) {
		super(pvInstance);
	}

/*	public void Enable(boolean enable) {
		HRESULT result = (HRESULT) this._invokeNativeObject(VTABLE_ID_ENABLE,
				new Object[] { this.getPointer(), new BOOL(enable) },
				HRESULT.class);
		COMUtils.checkRC(result);
	}*/

/*	public DESKTOP_WALLPAPER_POSITION GetPosition() {
		IntByReference resultValue = new IntByReference();
		HRESULT result = (HRESULT) this._invokeNativeObject(VTABLE_ID_GET_POSITION,
				new Object[]{this.getPointer(), resultValue},
				HRESULT.class);
		COMUtils.checkRC(result);
		return EnumUtils.fromInteger(resultValue.getValue(), DESKTOP_WALLPAPER_POSITION.class);
	}*/

/*	public void SetPosition(DESKTOP_WALLPAPER_POSITION position) {
		HRESULT result = (HRESULT) this._invokeNativeObject(VTABLE_ID_SET_POSITION,
				new Object[]{this.getPointer(), EnumUtils.toInteger(position)},
				HRESULT.class);
		COMUtils.checkRC(result);
	}*/

	public int GetMonitorDevicePathCount() {
		IntByReference resultValue = new IntByReference();
		HRESULT result = (HRESULT) this._invokeNativeObject(VTABLE_ID_GET_MONITOR_DEVICE_PATH_COUNT,
				new Object[]{this.getPointer(), resultValue},
				HRESULT.class);
		COMUtils.checkRC(result);
		return resultValue.getValue();
	}

	public String GetMonitorDevicePathAt(int monitorIdx) {
		PointerByReference pbr = new PointerByReference();
		HRESULT result = (HRESULT) this._invokeNativeObject(VTABLE_ID_GET_MONITOR_DEVICE_PATH_AT,
				new Object[]{this.getPointer(), monitorIdx, pbr},
				HRESULT.class);
		COMUtils.checkRC(result);
		if (pbr.getValue() != null) {
			try {
				return pbr.getValue().getWideString(0);
			} finally {
				Ole32.INSTANCE.CoTaskMemFree(pbr.getValue());
			}
		} else {
			return null;
		}
	}

/*	private static final int RECT_SIZE = new RECT().size();
	public RECT GetMonitorRect(String monitorPath) {
		// Allocate memory for the rect (the documentation says, that the
		// parameter is a pointer to the structure where the result will be
		// placed
		Memory mem = new Memory(RECT_SIZE);
		// the monitorPath is wrapped into a WString, so that the bindings
		// "know", that the string needs to be converted into an array of
		// wchars and not chars.
		HRESULT result = (HRESULT) this._invokeNativeObject(VTABLE_ID_GET_MONITOR_RECT,
				new Object[]{this.getPointer(), new WString(monitorPath), mem},
				HRESULT.class);
		COMUtils.checkRC(result);
		// Wrap the memory buffer into a RECT struct and ensure the java side
		// is up-to-date
		RECT resultValue = Structure.newInstance(RECT.class, mem);
		resultValue.read();
		return resultValue;
	}*/

/*	public String GetWallpaper(int monitorIdx) {
		// The documentation says, that the second parameter is a pointer to a
		// LPWSTR, we will allocate a pointer sized buffer, where the pointer
		// to the wchar array will be placed
		PointerByReference pbr = new PointerByReference();
		HRESULT result = (HRESULT) this._invokeNativeObject(VTABLE_ID_GET_WALLPAPER,
				new Object[]{this.getPointer(), monitorIdx, pbr},
				HRESULT.class);
		COMUtils.checkRC(result);
		if (pbr.getValue() != null) {
			try {
				return pbr.getValue().getWideString(0);
			} finally {
				// The server allocated the string, we need to free it
				Ole32.INSTANCE.CoTaskMemFree(pbr.getValue());
			}
		} else {
			return null;
		}
	}*/

	public void SetWallpaper(String monitorIdx, String path) {
		WString wMonitorIdx = new WString(monitorIdx);
		WString wstring = new WString(path);
		HRESULT result = (HRESULT) this._invokeNativeObject(VTABLE_ID_SET_WALLPAPER,
				new Object[]{this.getPointer(), wMonitorIdx, wstring},
				HRESULT.class);
		COMUtils.checkRC(result);
	}
}