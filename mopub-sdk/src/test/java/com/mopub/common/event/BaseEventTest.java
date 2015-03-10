package com.mopub.common.event;

import com.mopub.common.ClientMetadata;
import com.mopub.common.test.support.SdkTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SdkTestRunner.class)
public class BaseEventTest {

    private BaseEvent subject;
    @Mock private ClientMetadata mockClientMetaData;

    @Before
    public void setUp() {
        when(mockClientMetaData.getSdkVersion()).thenReturn("sdk_version");
        when(mockClientMetaData.getAppName()).thenReturn("app_name");
        when(mockClientMetaData.getAppPackageName()).thenReturn("app_package_name");
        when(mockClientMetaData.getAppVersion()).thenReturn("app_version");
        when(mockClientMetaData.getDeviceId()).thenReturn("client_device_id");
        when(mockClientMetaData.isDoNotTrackSet()).thenReturn(true);
        when(mockClientMetaData.getDeviceManufacturer()).thenReturn("device_manufacturer");
        when(mockClientMetaData.getDeviceModel()).thenReturn("device_model");
        when(mockClientMetaData.getDeviceProduct()).thenReturn("device_product");
        when(mockClientMetaData.getDeviceOsVersion()).thenReturn("device_os_version");
        when(mockClientMetaData.getDeviceScreenWidthPx()).thenReturn(1337);
        when(mockClientMetaData.getDeviceScreenHeightPx()).thenReturn(70707);
        when(mockClientMetaData.getActiveNetworkType()).thenReturn(ClientMetadata.MoPubNetworkType.WIFI);
        when(mockClientMetaData.getNetworkOperator()).thenReturn("network_operator");
        when(mockClientMetaData.getNetworkOperatorName()).thenReturn("network_operator_name");
        when(mockClientMetaData.getIsoCountryCode()).thenReturn("network_iso_country_code");
        when(mockClientMetaData.getSimOperator()).thenReturn("network_sim_operator");
        when(mockClientMetaData.getSimOperatorName()).thenReturn("network_sim_operator_name");
        when(mockClientMetaData.getSimIsoCountryCode()).thenReturn("network_sim_iso_country_code");

        subject = new Event.Builder("name", "category")
                .withSdkProduct(BaseEvent.SdkProduct.NATIVE)
                .withAdUnitId("8cf00598d3664adaaeccd800e46afaca")
                .withAdCreativeId("3c2b887e2c2a4cd0ae6a925440a62f0d")
                .withAdType("html")
                .withAdNetworkType("admob")
                .withAdWidthPx(320.0)
                .withAdHeightPx(50.0)
                .withGeoLat(37.7833)
                .withGeoLon(-122.4183333)
                .withGeoAccuracy(10.0)
                .withPerformanceDurationMs(100.0)
                .withRequestId("b550796074da4559a27c5072dcba2b27")
                .withRequestStatusCode(200)
                .withRequestUri("http://ads.mopub.com/m/ad?id=8cf00598d3664adaaeccd800e46afaca")
                .withRequestRetries(0)
                .build();
        subject.setClientMetaData(mockClientMetaData);
    }

    @Test
    public void constructor_shouldCorrectlyAssignFieldsFromBuilder() throws Exception {
        assertThat(subject.getSdkProduct()).isEqualTo(BaseEvent.SdkProduct.NATIVE);
        assertThat(subject.getAdUnitId()).isEqualTo("8cf00598d3664adaaeccd800e46afaca");
        assertThat(subject.getAdCreativeId()).isEqualTo("3c2b887e2c2a4cd0ae6a925440a62f0d");
        assertThat(subject.getAdType()).isEqualTo("html");
        assertThat(subject.getAdNetworkType()).isEqualTo("admob");
        assertThat(subject.getAdWidthPx()).isEqualTo(320.0);
        assertThat(subject.getAdHeightPx()).isEqualTo(50.0);
        assertThat(subject.getGeoLat()).isEqualTo(37.7833);
        assertThat(subject.getGeoLon()).isEqualTo(-122.4183333);
        assertThat(subject.getGeoAccuracy()).isEqualTo(10.0);
        assertThat(subject.getPerformanceDurationMs()).isEqualTo(100.0);
        assertThat(subject.getRequestId()).isEqualTo("b550796074da4559a27c5072dcba2b27");
        assertThat(subject.getRequestStatusCode()).isEqualTo(200);
        assertThat(subject.getRequestUri()).isEqualTo("http://ads.mopub.com/m/ad?id=8cf00598d3664adaaeccd800e46afaca");
        assertThat(subject.getRequestRetries()).isEqualTo(0);
    }

    @Test
    public void getSdkVersion_shouldReturnClientMetaDataSdkVersion() throws Exception {
        assertThat(subject.getSdkVersion()).isEqualTo("sdk_version");
    }

    @Test
    public void getAppName_shouldReturnClientMetaDataAppName() throws Exception {
        assertThat(subject.getAppName()).isEqualTo("app_name");
    }

    @Test
    public void getAppPackageName_shouldReturnClientMetaDataAppPackageName() throws Exception {
        assertThat(subject.getAppPackageName()).isEqualTo("app_package_name");
    }

    @Test
    public void getAppVersion_shouldReturnClientMetaDataAppVersion() throws Exception {
        assertThat(subject.getAppVersion()).isEqualTo("app_version");
    }

    @Test
    public void getClientAdvertisingId_shouldReturnClientMetaDataDeviceId() throws Exception {
        assertThat(subject.getClientAdvertisingId()).isEqualTo("client_device_id");
    }

    @Test
    public void getClientDoNotTrack_shouldReturnClientMetaDataDoNotTrack() throws Exception {
        assertThat(subject.getClientDoNotTrack()).isEqualTo(true);
    }

    @Test
    public void getDeviceManufacturer_shouldReturnClientMetaDataDeviceManufacturer() throws Exception {
        assertThat(subject.getDeviceManufacturer()).isEqualTo("device_manufacturer");
    }

    @Test
    public void getDeviceModel_shouldReturnClientMetaDataDeviceModel() throws Exception {
        assertThat(subject.getDeviceModel()).isEqualTo("device_model");
    }

    @Test
    public void getDeviceProduct_shouldReturnClientMetaDataDeviceProduct() throws Exception {
        assertThat(subject.getDeviceProduct()).isEqualTo("device_product");
    }

    @Test
    public void getDeviceOsVersion_shouldReturnClientMetaDataDeviceOsVersion() throws Exception {
        assertThat(subject.getDeviceOsVersion()).isEqualTo("device_os_version");
    }

    @Test
    public void getDeviceScreenWidthPx_shouldReturnClientMetaDataDeviceScreenWidthPx() throws Exception {
        assertThat(subject.getDeviceScreenWidthPx()).isEqualTo(1337);
    }

    @Test
    public void getDeviceScreenHeightPx_shouldReturnClientMetaDataDeviceScreenHeightPx() throws Exception {
        assertThat(subject.getDeviceScreenHeightPx()).isEqualTo(70707);
    }

    @Test
    public void getNetworkType_shouldReturnClientMetaDataActiveNetworkType() throws Exception {
        assertThat(subject.getNetworkType()).isEqualTo(ClientMetadata.MoPubNetworkType.WIFI);
    }

    @Test
    public void getNetworkOperatorCode_shouldReturnClientMetaDataNetworkOperator() throws Exception {
        assertThat(subject.getNetworkOperatorCode()).isEqualTo("network_operator");
    }

    @Test
    public void getNetworkOperatorName_shouldReturnClientMetaDataNetworkOperatorName() throws Exception {
        assertThat(subject.getNetworkOperatorName()).isEqualTo("network_operator_name");
    }

    @Test
    public void getNetworkIsoCountryCode_shouldReturnClientMetaDataNetworkIsoCountryCode() throws Exception {
        assertThat(subject.getNetworkIsoCountryCode()).isEqualTo("network_iso_country_code");
    }

    @Test
    public void getNetworkSimCode_shouldReturnClientMetaDataNetworkSimOperator() throws Exception {
        assertThat(subject.getNetworkSimCode()).isEqualTo("network_sim_operator");
    }

    @Test
    public void getNetworkSimOperatorName_shouldReturnClientMetaDataNetworkSimOperatorName() throws Exception {
        assertThat(subject.getNetworkSimOperatorName()).isEqualTo("network_sim_operator_name");
    }

    @Test
    public void getNetworkSimIsoCountryCode_shouldReturnClientMetaDataNetworkSimIsoCountryCode() throws Exception {
        assertThat(subject.getNetworkSimIsoCountryCode()).isEqualTo("network_sim_iso_country_code");
    }
}
