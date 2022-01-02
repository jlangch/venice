/*   __    __         _
 *   \ \  / /__ _ __ (_) ___ ___ 
 *    \ \/ / _ \ '_ \| |/ __/ _ \
 *     \  /  __/ | | | | (_|  __/
 *      \/ \___|_| |_|_|\___\___|
 *
 *
 * Copyright 2017-2022 Venice
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
 */
package com.github.jlangch.venice.support;

import java.beans.Transient;



public class AuditEvent {

	public AuditEvent() {
	}

	public AuditEvent(
			final String principal,
			final long elapsedTimeMillis,
			final AuditEventType eventType,
			final String eventKey,
			final String eventName,
			final String eventMessage
	) {
		this.principal = principal;
		this.elapsedTimeMillis = elapsedTimeMillis;
		this.eventType = eventType;
		this.eventKey = eventKey;
		this.eventName = eventName;
		this.eventMessage = eventMessage;
	}

	public String getPrincipal() {
		return principal;
	}
	public void setPrincipal(final String principal) {
		this.principal = principal;
	}

	public long getElapsedTimeMillis() {
		return elapsedTimeMillis;
	}
	public void setElapsedTimeMillis(final Integer elapsedTimeMillis) {
		this.elapsedTimeMillis = elapsedTimeMillis;
	}

	public AuditEventType getEventType() {
		return eventType;
	}
	public void setEventType(final AuditEventType eventType) {
		this.eventType = eventType;
	}
	@Transient
	public boolean isType(final AuditEventType ... type) {
		if (type != null) {
			for(AuditEventType t : type) {
				if (getEventType() == t) return true;
			}
		}
		return false;
	}

	public String getEventName() {
		return eventName;
	}
	public void setEventName(final String eventName) {
		this.eventName = eventName;
	}

	public String getEventKey() {
		return eventKey;
	}
	public void setEventKey(final String eventKey) {
		this.eventKey = eventKey;
	}

	public String getEventMessage() {
		return eventMessage;
	}
	public void setEventMessage(final String eventMessage) {
		this.eventMessage = eventMessage;
	}

	
	private String principal;
	private long elapsedTimeMillis;
	private AuditEventType eventType;
	private String eventName;
	private String eventKey;
	private String eventMessage;
}
