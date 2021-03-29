/*
 * Copyright 2014 NAVER Corp.
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
 */
package com.navercorp.pinpoint.common.trace;


/**
 * @author Jongho Moon
 *
 */
public enum ServiceTypeProperty {
    TERMINAL,
    QUEUE,
    RECORD_STATISTICS,
    INCLUDE_DESTINATION_ID,  // 记录了目标ID和remote service是不可追踪的类型
    ALIAS
}
