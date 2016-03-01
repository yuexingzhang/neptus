/*
 * Copyright (c) 2004-2016 Universidade do Porto - Faculdade de Engenharia
 * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
 * All rights reserved.
 * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
 *
 * This file is part of Neptus, Command and Control Framework.
 *
 * Commercial Licence Usage
 * Licencees holding valid commercial Neptus licences may use this file
 * in accordance with the commercial licence agreement provided with the
 * Software or, alternatively, in accordance with the terms contained in a
 * written agreement between you and Universidade do Porto. For licensing
 * terms, conditions, and further information contact lsts@fe.up.pt.
 *
 * European Union Public Licence - EUPL v.1.1 Usage
 * Alternatively, this file may be used under the terms of the EUPL,
 * Version 1.1 only (the "Licence"), appearing in the file LICENSE.md
 * included in the packaging of this file. You may not use this work
 * except in compliance with the Licence. Unless required by applicable
 * law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific
 * language governing permissions and limitations at
 * https://www.lsts.pt/neptus/licence.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: tsmarques
 * 15 Feb 2016
 */
package pt.lsts.neptus.plugins.mvplanning;

import pt.lsts.imc.PlanSpecification;
import pt.lsts.imc.types.PlanSpecificationAdapter;
import pt.lsts.neptus.plugins.mvplanning.jaxb.Profile;

/**
 * @author tsmarques
 */

/* Wrapper around PlanSpecification */
public class PlanTask {
    private String planId;
    private PlanSpecification plan;
    private Profile planProfile;
    private double timestamp;

    public PlanTask(String id, PlanSpecification plan, Profile planProfile, double timestamp) {
        this.planId = id;
        this.plan = plan;
        this.planProfile = planProfile;
        this.timestamp = timestamp;
    }
    
    public String getPlanId() {
        return planId;
    }

    public PlanSpecification getPlanSpecification() {
        return this.plan;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public Profile getProfile() {
        return planProfile;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public void setPlan(PlanSpecification imcPlan) {
       this.plan = imcPlan;
    }

    /**
     * If the given vehicle can execute this plan with
     * the needed profile.
     * */
    public boolean containsVehicle(String vehicle) {
        return planProfile.getProfileVehicles().contains(vehicle);
    }
}
