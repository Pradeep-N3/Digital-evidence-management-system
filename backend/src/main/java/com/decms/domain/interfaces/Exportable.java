package com.decms.domain.interfaces;

import java.util.Map;

public interface Exportable {
    Map<String, Object> getExportMetadata();
    String getExportTitle();
}
